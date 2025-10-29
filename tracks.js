const tdl = require('tdl')
const { getTdjson } = require('prebuilt-tdlib')
tdl.configure({ tdjson: getTdjson() })

require('dotenv').config()

const fs = require("fs")
const path = require('path')
const os = require('os')

const client = tdl.createClient({
    apiId: process.env.API_ID,
    apiHash: process.env.API_HASH,
})

const MUSIC_DIRECTORY = path.join(__dirname, '_td_files', 'music')
const LIQUIDSOAP_SCRIPT_DIR = os.platform() === 'linux' ? '/etc/liquidsoap' : path.join(__dirname, 'liquidsoap')
const QUEUE_FILE = path.join(LIQUIDSOAP_SCRIPT_DIR, 'trackQueue.json')

const GET_TRACKS_FROM_CHANNEL_TIMEOUT = 60 * 60 * 1000      //1h
const DELETE_UNUSED_TRACKS_TIMEOUT = 10 * 60 * 1000         //10 min
const FULL_QUEUE_TIMEOUT = 5 * 60 * 1000                    //5 min
const GET_RANDOM_TRACK_TIMEOUT = 2 * 60 * 1000              //2 min
const UNIVERSAL_RETRY_TIMEOUT = 30 * 1000                   //30 sec

let tracks = []

let trackQueue = []

let isInitializing
let _saveTimer = null

const saveQueue = (delay = 500) => {
    if (_saveTimer) clearTimeout(_saveTimer)
    _saveTimer = setTimeout(async () => {
        try {
            if (!fs.existsSync(LIQUIDSOAP_SCRIPT_DIR)) fs.mkdirSync(LIQUIDSOAP_SCRIPT_DIR, { recursive: true })
            await fs.promises.writeFile(QUEUE_FILE, JSON.stringify(trackQueue, null, 2), 'utf8')
        } catch (e) {
            console.log('Error saving queue:', e.message)
        }
        _saveTimer = null
    }, delay)
}

const loadQueue = () => {
    try {
        if (!fs.existsSync(MUSIC_DIRECTORY)) {
            fs.mkdirSync(MUSIC_DIRECTORY, { recursive: true })
        }
        if (!fs.existsSync(LIQUIDSOAP_SCRIPT_DIR)) {
            fs.mkdirSync(LIQUIDSOAP_SCRIPT_DIR, { recursive: true })
        }

        if (fs.existsSync(QUEUE_FILE)) {
            const raw = fs.readFileSync(QUEUE_FILE, 'utf8')
            const parsed = JSON.parse(raw)
            if (Array.isArray(parsed)) {
                trackQueue = parsed.filter(entry => {
                    if (!entry || !entry.fileName) return false
                    const filePath = path.join(MUSIC_DIRECTORY, entry.fileName)
                    const exists = fs.existsSync(filePath)
                    if (!exists) console.log(`Queued file missing on disk, skipping: ${entry.fileName}`)
                    return exists
                }).map(e => ({ ...e, isScheduled: false })) // clear scheduled flags on startup
            }
        }
    } catch (e) {
        console.log('Error loading queue:', e.message)
    }
}


const connect = async () => {
    client.on('error', err => console.error(`Client error: ${err}`))
    await client.login({
        async getPhoneNumber(retry) {
            if (retry) throw new Error('Invalid phone number')
            return process.env.PHN
        },
    })
    console.log('Telegram connected.')

    const me = await client.invoke({ '@type': 'getMe' })
    console.log(`Logged in as ${me.first_name}`)
    isInitializing = true
}


const getTracksFromChannel = async (channelUsername) => {
    const channel = await client.invoke({
        '@type': 'searchPublicChat',
        username: channelUsername.replace('@', '')
    })

    console.log(`Getting tracks from ${channelUsername}`)

    const fetchedTracks = []
    let offsetId = 0
    let limit = 50

    while (true) {
        const history = await client.invoke({
            '@type': 'getChatHistory',
            chat_id: channel.id,
            from_message_id: offsetId,
            offset: 0,
            limit: limit,
            only_local: false
        })

        if (!history.messages.length) break

        for (const msg of history.messages) {
            const content = msg.content
            if (
                content['_'] === 'messageAudio'
            ) {
                const audioAttr = content.audio

                fetchedTracks.push({
                    id: msg.id,
                    title: audioAttr?.title ?? 'Untitled',
                    performer: audioAttr?.performer ?? 'Unknown',
                    caption: content.caption.text,
                    fileName: audioAttr.file_name,
                    duration: audioAttr?.duration ?? 0,
                    size: audioAttr?.audio?.size,
                    file_id: audioAttr?.audio?.id,
                    cover_id: audioAttr?.album_cover_thumbnail?.file.id,
                })
            }
        }

        offsetId = history.messages[history.messages.length - 1].id
        await new Promise(r => setTimeout(r, 500))
    }

    tracks = fetchedTracks

    setTimeout(() => {
        getTracksFromChannel(channelUsername)
    }, GET_TRACKS_FROM_CHANNEL_TIMEOUT)
}

const getTrackFullName = (track) => {
    if (!track) return 'Unknown'
    if (track.caption) {
        const idx = track.caption.indexOf('\n')
        const firstLine = idx === -1 ? track.caption : track.caption.substring(0, idx)
        return firstLine.trim()
    }

    const hasTitle = track.title && String(track.title).trim().length > 0
    const hasPerformer = track.performer && String(track.performer).trim().length > 0

    if (hasTitle || hasPerformer) {
        const performer = hasPerformer ? String(track.performer).trim() : '<Неизвестен>'
        const title = hasTitle ? String(track.title).trim() : '<Без названия>'
        return `${performer} - ${title}`
    }

    if (track.fileName && track.fileName.lastIndexOf('.') !== -1) {
        return track.fileName.substring(0, track.fileName.lastIndexOf('.'))
    }

    return 'Unknown'
}

const downloadTrack = async (fileId) => {

    let a = await client.invoke({
        _: "downloadFile",
        file_id: fileId,
        priority: 32,
        synchronous: true,
    })

    let fileName = Date.now() + a.local.path.substring(a.local.path.lastIndexOf("."))

    try {
        fs.renameSync(a.local.path, path.join(MUSIC_DIRECTORY, fileName))
    } catch (err) {
        console.log(`Error when renaming: ${err.message}`)
    }

    console.log(`Track saved as ${fileName}`)
    return fileName
}

const downloadTrackCover = async (fileId, trackFileName) => {
    if (!fileId) return null

    let a = await client.invoke({
        _: "downloadFile",
        file_id: fileId,
        priority: 32,
        synchronous: true,
    })

    let fileName = trackFileName + "_cover" + a.local.path.substring(a.local.path.lastIndexOf("."))

    try {
        fs.renameSync(a.local.path, path.join(MUSIC_DIRECTORY, fileName))
    } catch (err) {
        console.log(`Error when renaming: ${err.message}`)
    }

    console.log(`Track cover saved as ${fileName}`)
    return fileName
}

const deleteUnusedTracks = async () => {
    console.log("gc running")
    const files = fs.readdirSync(MUSIC_DIRECTORY)
    console.log(files)
    for (f of files) {
        if (!trackQueue.map(e => e.fileName).includes(f.replace(/(_cover).*$/, ""))) {
            console.log(`File ${f} not presented in queue. Deleting`)
            try {
                fs.unlinkSync(path.join(MUSIC_DIRECTORY, f))
                console.log(`${f} deleted`)
            } catch (err) {
                console.log(`Error when deleting: ${err.message}`)
            }
        }
    }
    setTimeout(() => {
        deleteUnusedTracks()
    }, DELETE_UNUSED_TRACKS_TIMEOUT)
}

const getRandomTrack = () => {

    if (trackQueue.length >= 10) {
        console.log("Queue Full, waiting for 5 mins")
        isInitializing = false
        setTimeout(() => {
            getRandomTrack()
        }, FULL_QUEUE_TIMEOUT)
        return
    }

    let randt = tracks[Math.floor(Math.random() * tracks.length)]
    console.log(randt.id)

    if (trackQueue.filter(t => t.isScheduled == false).find(t => t.postId === randt.id)) {
        console.log(`Track ${getTrackFullName(randt)} already in queue. Retrying`)
        setTimeout(() => {
            getRandomTrack()
        }, UNIVERSAL_RETRY_TIMEOUT)
        return
    }

    downloadTrack(randt.file_id).then(async (fileName) => {
        coverFileName = await downloadTrackCover(randt.cover_id, fileName)
        trackQueue.push({
            postId: randt.id,
            fileName: fileName,
            coverFileName: coverFileName,
            isScheduled: false,
        })

        saveQueue()

        setTimeout(() => {
            getRandomTrack()
        }, isInitializing ? UNIVERSAL_RETRY_TIMEOUT : GET_RANDOM_TRACK_TIMEOUT)

    }).catch(e => {
        console.log(e)
        setTimeout(() => {
            getRandomTrack()
        }, UNIVERSAL_RETRY_TIMEOUT)
    })
}

exports.run = async () => {
    loadQueue()

    connect().then(async () => {
        await getTracksFromChannel(process.env.CHANNEL)

        getRandomTrack()

    })

    deleteUnusedTracks()

}

exports.removeTrackFromQueue = () => {
    if (trackQueue.length > 0) {
        let f = trackQueue.shift()
        console.log(`Track ${tracks.find(track => track.id === f.postId)?.fileName} removed from queue`)
        saveQueue()
    }
}

exports.getQueueLength = () => trackQueue.filter(t => t.isScheduled == false).length
exports.getNextTrack = () => trackQueue.find(t => t.isScheduled == false)
exports.getTrackMetadata = (fileName) => tracks.find(t => t.id == trackQueue.find(t => t.fileName == fileName).postId)
exports.getTrackTitle = (fileName) => getTrackFullName(this.getTrackMetadata(fileName))
exports.scheduleTrack = (fileName) => {
    const entry = trackQueue.find(t => t.fileName == fileName)
    if (entry) {
        entry.isScheduled = true
        saveQueue()
        return true
    }
    return false
}