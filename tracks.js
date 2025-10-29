const tdl = require('tdl')
const { getTdjson } = require('prebuilt-tdlib')
tdl.configure({ tdjson: getTdjson() })

require('dotenv').config()

const fs = require("fs")

const client = tdl.createClient({
    apiId: process.env.API_ID,
    apiHash: process.env.API_HASH,
})

const MUSIC_DIRECTORY = __dirname + "\\_td_files\\music\\"

const GET_TRACKS_FROM_CHANNEL_TIMEOUT = 60 * 60 * 1000      //1h
const DELETE_UNUSED_TRACKS_TIMEOUT = 10 * 60 * 1000         //10 min
const FULL_QUEUE_TIMEOUT = 5 * 60 * 1000                    //5 min
const GET_RANDOM_TRACK_TIMEOUT = 2 * 60 * 1000              //2 min
const UNIVERSAL_RETRY_TIMEOUT = 30 * 1000                   //30 sec

let tracks = []

let trackQueue = []

let isInitializing

connect = async () => {
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


getTracksFromChannel = async (channelUsername) => {
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

getTrackFullName = (track) => (track.caption.substring(0, track.caption.indexOf("\n"))) ? track.caption
    : (track.title && track.performer) ? `${track.performer} - ${track.title}`
        : (track.fileName.substring(0, track.fileName.lastIndexOf(".")))

downloadTrack = async (fileId) => {

    let a = await client.invoke({
        _: "downloadFile",
        file_id: fileId,
        priority: 32,
        synchronous: true,
    })

    let fileName = Date.now() + a.local.path.substring(a.local.path.lastIndexOf("."))

    fs.rename(a.local.path, MUSIC_DIRECTORY + fileName, err => {
        if (err)
            console.log(`Error when renaming: ${err.message}`)
    })

    console.log(`Track saved as ${fileName}`)
    return fileName
}

downloadTrackCover = async (fileId, trackFileName) => {
    if (!fileId) return null

    let a = await client.invoke({
        _: "downloadFile",
        file_id: fileId,
        priority: 32,
        synchronous: true,
    })

    let fileName = trackFileName + "_cover" + a.local.path.substring(a.local.path.lastIndexOf("."))

    fs.rename(a.local.path, MUSIC_DIRECTORY + fileName, err => {
        if (err)
            console.log(`Error when renaming: ${err.message}`)
    })

    console.log(`Track cover saved as ${fileName}`)
    return fileName
}

deleteUnusedTracks = async () => {
    console.log("gc running")
    const files = fs.readdirSync(MUSIC_DIRECTORY)
    console.log(files)
    for (f of files) {
        if (!trackQueue.map(e => e.fileName).includes(f.replace(/(_cover).*$/, ""))) {
            console.log(`File ${f} not presented in queue. Deleting`)
            await fs.unlink(MUSIC_DIRECTORY + f, err => { console.log(err ? "Error when deleting: " + err.message : `${f} deleted`) })
        }
    }
    setTimeout(() => {
        deleteUnusedTracks()
    }, DELETE_UNUSED_TRACKS_TIMEOUT)
}

getRandomTrack = () => {

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
    }
}

exports.getQueueLength = () => trackQueue.filter(t => t.isScheduled == false).length
exports.getNextTrack = () => trackQueue.find(t => t.isScheduled == false)
exports.getTrackMetadata = (fileName) => tracks.find(t => t.id == trackQueue.find(t => t.fileName == fileName).postId)
exports.getTrackTitle = (fileName) => getTrackFullName(this.getTrackMetadata(fileName))
exports.scheduleTrack = (fileName) => trackQueue.find(t => t.fileName == fileName).isScheduled = true