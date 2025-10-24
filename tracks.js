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

let t = []

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
    this.isInitializing = true
}


getTracksFromChannel = async (channelUsername) => {
    const channel = await client.invoke({
        '@type': 'searchPublicChat',
        username: channelUsername.replace('@', '')
    })

    console.log(`Getting tracks from ${channelUsername}`)

    const tracks = []
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

                tracks.push({
                    id: msg.id,
                    title: audioAttr?.title ?? 'Untitled',
                    performer: audioAttr?.performer ?? 'Unknown',
                    fullName: content.caption.text ?? `${audioAttr?.performer ?? 'Unknown'} - ${audioAttr?.title ?? 'Untitled'}`,
                    fileName: audioAttr.file_name,
                    duration: audioAttr?.duration ?? 0,
                    size: audioAttr?.audio?.size,
                    file_id: audioAttr?.audio?.id
                })
            }
        }

        offsetId = history.messages[history.messages.length - 1].id
        await new Promise(r => setTimeout(r, 500))
    }

    //saveTracks(tracks)

    t = tracks

    setTimeout(() => {
        getTracksFromChannel(channelUsername)
    }, 60 * 60 * 1000) //1h
}

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

exports.removeTrackFromQueue = () => {
    if (trackQueue.length > 0) {
        let f = trackQueue.shift()
        console.log(`Track ${t.find(track => track.id === f.postId)?.fileName} removed from queue`)
    }
}

exports.getQueueLength = () => trackQueue.filter(t => t.isScheduled == false).length
exports.getNextTrack = () => trackQueue.find(t => t.isScheduled == false)
exports.scheduleTrack = (fileName) => trackQueue.find(t => t.fileName == fileName).isScheduled = true

deleteUnusedTracks = async () => {
    console.log("gc running")
    const files = fs.readdirSync(MUSIC_DIRECTORY)
    console.log(files)
    for (f of files) {
        if (!trackQueue.map(e => e.fileName).includes(f)) {
            console.log(`File ${f} not presented in queue. Deleting`)
            await fs.unlink(MUSIC_DIRECTORY + f, err => { console.log(err ? "Error when deleting: " + err.message : `${f} deleted`) })
        }
    }
    setTimeout(() => {
        deleteUnusedTracks()
    }, 10 * 60 * 1000)
}

getRandomTrack = () => {

    if (trackQueue.length >= 10) {
        console.log("Queue Full, waiting for 5 mins")
        this.isInitializing = false
        setTimeout(() => {
            getRandomTrack()
        }, 5 * 60 * 1000)
        return
    }
    //let t = JSON.parse(fs.readFileSync(tracksFileName));

    let randt = t[Math.floor(Math.random() * t.length)]
    console.log(randt.id)
    downloadTrack(randt.file_id).then(async (fileName) => {
        trackQueue.push({
            postId: randt.id,
            fileName: fileName,
            isScheduled: false,
        })

        setTimeout(() => {
            getRandomTrack()
        }, (this.isInitializing ? 30 : 2 * 60) * 1000) //2min, or 30s if initializing

    }).catch(e => {
        console.log(e)
        setTimeout(() => {
            getRandomTrack()
        }, 30 * 1000) //30s
    })
}

exports.run = async () => {
    connect().then(async () => {
        await getTracksFromChannel(process.env.CHANNEL)

        getRandomTrack()

    })

    deleteUnusedTracks()
}