const fs = require("fs")
const path = require('path')
const os = require('os')
const { spawn } = require('child_process')

require('dotenv').config({ path: path.resolve(__dirname, "../../.env") })

const tdl = require('tdl')
const { getTdjson } = require('prebuilt-tdlib')
tdl.configure({ tdjson: getTdjson() })

const TrackRepository = require(path.resolve(process.env.SHARED_DB_DIR, "repositories/TrackRepository"))
const QueueRepository = require(path.resolve(process.env.SHARED_DB_DIR, "repositories/QueueRepository"))

const FFMPEG_PATH = os.platform() === 'win32'
    ? path.join("C:\\ffmpeg\\bin", 'ffmpeg.exe')
    : 'ffmpeg'

const client = tdl.createClient({
    apiId: parseInt(process.env.API_ID),
    apiHash: process.env.API_HASH,
})

const MUSIC_DIRECTORY = path.join(__dirname, '_td_files', 'music')
const LINUX_MUSIC_DIRECTORY = "/app/music"

const GET_TRACKS_FROM_CHANNEL_TIMEOUT = 60 * 60 * 1000      //1h
const DELETE_UNUSED_TRACKS_TIMEOUT = 10 * 60 * 1000         //10 min
const FULL_QUEUE_TIMEOUT = 5 * 60 * 1000                    //5 min
const GET_RANDOM_TRACK_TIMEOUT = 2 * 60 * 1000              //2 min
const UNIVERSAL_RETRY_TIMEOUT = 30 * 1000                   //30 sec

let isInitializing

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
    try {
        const channel = await client.invoke({
            '@type': 'searchPublicChat',
            username: channelUsername.replace('@', '')
        })

        console.log(`Getting tracks from ${channelUsername}`)

        let offsetId = 0
        let limit = 50
        let tracksProcessed = 0

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

                    const trackData = {
                        telegram_id: msg.id,
                        title: audioAttr.title || 'Untitled',
                        performer: audioAttr.performer || 'Unknown',
                        performerString: audioAttr.performer,
                        caption: content.caption?.text || null,
                        file_name: audioAttr.file_name || `${audioAttr.title || 'track'}.mp3`,
                        duration: audioAttr.duration || 0,
                        file_size: audioAttr.audio.size || 0,
                        telegram_file_id: audioAttr.audio?.remote?.id || null,
                        telegram_cover_id: audioAttr.album_cover_thumbnail?.file?.remote?.id || null,
                    }

                    try {
                        const track = await TrackRepository.createTrackWithArtists(trackData)
                        if (track) {
                            tracksProcessed++
                            console.log(`Track saved to database: ${trackData.title} by ${trackData.performer}`)
                        }
                    } catch (error) {
                        if (!error.message.includes('duplicate key')) {
                            console.error('Error saving track to database:', error)
                        }
                    }

                }
            }

            offsetId = history.messages[history.messages.length - 1].id
            await new Promise(r => setTimeout(r, 500))
        }

        console.log(`Processed ${tracksProcessed} tracks from ${channelUsername}`)

        setTimeout(() => {
            getTracksFromChannel(channelUsername)
        }, GET_TRACKS_FROM_CHANNEL_TIMEOUT)
    } catch (e) {
        console.log('Error fetching tracks from channel:', e.message)
        setTimeout(() => {
            getTracksFromChannel(channelUsername)
        }, UNIVERSAL_RETRY_TIMEOUT)
    }
}

const getTrackCredits = (track) => {
    if (!track) return { author: '<Неизвестен>', title: '<Трек не найден>' }

    if (track.caption) {
        const idx = track.caption.indexOf('\n')
        const firstLine = idx === -1 ? track.caption : track.caption.substring(0, idx)
        const creds = firstLine.split(/( [–-] )/)
        console.log("cap", creds)
        return { author: creds[0].trim(), title: creds[2].trim() }
    }

    const hasTitle = track.title && track.title.trim().length > 0
    const hasArtists = track.artists && track.artists.length > 0
        && track.artists.map(artist => artist.name).join(', ').trim().length > 0

    if (hasTitle || hasArtists) {
        const performer = hasArtists ? track.artists.map(artist => artist.name).join(', ').trim() : '<Неизвестен>'
        const title = hasTitle ? track.title.trim() : '<Без названия>'
        console.log("meta", performer, title)
        return { author: performer, title: title }
    }

    if (track.fileName && track.fileName.lastIndexOf('.') !== -1) {
        const creds = track.fileName.substring(0, track.fileName.lastIndexOf('.')).split(/( [–-] )/)
        console.log("file", creds)
        return { author: creds[0].trim(), title: creds[2].trim() }
    }

    return { author: '<Неизвестен>', title: '<Трек не распознан>' }
}

const getTrackFullName = (track) => {
    const { author, title } = getTrackCredits(track)
    return `${author} - ${title}`
}

const fixTrackMetadata = async (fileId, fileName) => {
    try {
        const track = await TrackRepository.findByFileId(fileId)

        if (!track) {
            console.log(`Track with file_id ${fileId} not found in database`)
            return false
        }

        const { author, title } = getTrackCredits(track)

        console.log(`Updating metadata for ${fileName}: ${author} - ${title}`)

        const tempPath = path.join(LINUX_MUSIC_DIRECTORY, fileName.substring(0, fileName.lastIndexOf("."))) + '_tmp' + fileName.substring(fileName.lastIndexOf("."))
        const filePath = path.join(LINUX_MUSIC_DIRECTORY, fileName)

        const isMp3 = fileName.toLowerCase().endsWith('.mp3')
        const duration = Math.max(30, Math.floor(track.duration || 0))

        const args = [
            '-i', filePath,
            '-c', 'copy',
            '-metadata', `title=${title}`,
            '-metadata', `artist=${author}`,
            '-metadata', `length=${duration}`,
            '-metadata', `duration=${duration}`,
            '-y',
            tempPath
        ]

        if (isMp3) {
            const durationMs = duration * 1000
            const minutes = Math.floor(duration / 60)
            const seconds = Math.floor(duration % 60)
            const timeTag = `${minutes}:${seconds.toString().padStart(2, '0')}`

            args.push(
                '-id3v2_version', '3',
                '-metadata', `TLEN=${durationMs}`,
                '-metadata', `TIME=${timeTag}`,
                '-metadata', `DURATION=${duration}`,
                '-metadata', `liq_duration=${duration}`,
                '-write_id3v1', '1'
            )
        } else {
            args.push(
                '-metadata', `DURATION=${duration}`,
                '-metadata', `liq_duration=${duration}`,
            )
        }

        const child = spawn(FFMPEG_PATH, args, {
            stdio: ["ignore", "pipe", "pipe"],
        })

        let stderr = ''
        child.stderr.on('data', (chunk) => (stderr += chunk.toString()))

        child.on('error', (err) => {
            console.error('[FFMpeg spawn error]', err && err.message ? err.message : err)
        })

        child.on("close", async (code) => {
            if (code === 0) {
                try {
                    fs.unlink(filePath, () => { })
                    fs.rename(tempPath, filePath, () => { })

                } catch (err) {
                    console.log(`Error replacing temp file: ${err}`)
                }
                console.log(`Metadata Updated: ${fileName}`)
            } else {
                console.error(`Error updating metadata for ${fileName}: ${stderr.trim() || 'unknown error'}`)
            }
        })
    } catch (error) {
        console.error(`Error in fixTrackMetadata for file ${fileName}:`, error)
    }
}

const downloadFile = async (remoteFileId, fileType = "fileTypeAudio", attempt = 1) => {
    try {
        const file = await client.invoke({
            "@type": "getRemoteFile",
            remote_file_id: remoteFileId,
            file_type: { "@type": fileType }
        })

        const downloaded = await client.invoke({
            _: "downloadFile",
            file_id: file.id,
            priority: 1,
            synchronous: true,
        })
        return downloaded.local.path

    } catch (err) {
        if (err.code === 400 && attempt <= 5) {
            await new Promise(r => setTimeout(r, 150 * attempt))
            return downloadFile(remoteFileId, fileType, attempt + 1)
        }
        throw err
    }
}

const downloadTrack = async (fileId) => {

    let filePath = await (downloadFile(fileId))

    let fileName = Date.now() + filePath.substring(filePath.lastIndexOf("."))

    try {
        fs.renameSync(filePath, path.join(MUSIC_DIRECTORY, fileName))
    } catch (err) {
        console.log(`Error when renaming: ${err.message}`)
    }

    console.log(`Track saved as ${fileName}`)
    await relocateFileToShared(fileName)
    await fixTrackMetadata(fileId, fileName)
    return fileName
}

const downloadTrackCover = async (fileId, trackFileName) => {
    if (!fileId) return null

    let filePath = await (downloadFile(fileId, "fileTypeThumbnail"))

    let fileName = trackFileName + "_cover" + filePath.substring(filePath.lastIndexOf("."))

    try {
        fs.renameSync(filePath, path.join(MUSIC_DIRECTORY, fileName))
    } catch (err) {
        console.log(`Error when renaming: ${err.message}`)
    }

    console.log(`Track cover saved as ${fileName}`)
    await relocateFileToShared(fileName)
    return fileName
}

const relocateFileToShared = (fileName) => {
    if (os.platform() === "linux") {
        fs.copyFileSync(path.join(MUSIC_DIRECTORY, fileName), path.join(LINUX_MUSIC_DIRECTORY, fileName))
        fs.unlinkSync(path.join(MUSIC_DIRECTORY, fileName))
        console.log(`Moved file ${fileName} from ${MUSIC_DIRECTORY} to ${LINUX_MUSIC_DIRECTORY}`)
    }
}

const deleteUnusedTracks = async () => {
    console.log("gc running")

    try {
        const { audioFiles, coverFiles } = await QueueRepository.getActiveQueueFiles()
        const workingDirectory = (os.platform() === "linux") ? LINUX_MUSIC_DIRECTORY : MUSIC_DIRECTORY
        const files = fs.readdirSync(workingDirectory)

        console.log(`Found ${files.length} files in directory`)
        console.log(`Active queue files: ${audioFiles.length} audio, ${coverFiles.length} covers`)

        for (const file of files) {
            const isAudioFile = !file.includes('_cover')
            const baseFileName = file.replace(/(_cover).*$/, "")

            if (isAudioFile) {
                if (!audioFiles.includes(baseFileName)) {
                    console.log(`File ${file} not present in queue. Deleting`)
                    try {
                        fs.unlinkSync(path.join(workingDirectory, file))
                        console.log(`${file} deleted`)
                    } catch (err) {
                        console.log(`Error deleting ${file}: ${err.message}`)
                    }
                }
            } else {
                if (!coverFiles.includes(file)) {
                    console.log(`Cover file ${file} not present in queue. Deleting`)
                    try {
                        fs.unlinkSync(path.join(workingDirectory, file))
                        console.log(`${file} deleted`)
                    } catch (err) {
                        console.log(`Error deleting cover ${file}: ${err.message}`)
                    }
                }
            }
        } false

        for (const trackFileName of audioFiles) {
            if (!files.includes(trackFileName)) {
                console.log(`Queue Item ${trackFileName} not present in filesystem. Marking as Failed`)
                await QueueRepository.updateQueueStatus(trackFileName, "failed")
            }
        }

        console.log("Cleaning Queue")
        let a = await QueueRepository.cleanupTrackQueue()
        console.log(`Removed ${a} items from Queue`)

        console.log("gc completed")

    } catch (error) {
        console.error("Error in deleteUnusedTracks:", error)
    }

    setTimeout(() => {
        deleteUnusedTracks()
    }, DELETE_UNUSED_TRACKS_TIMEOUT)
}

const getRandomTrack = async () => {
    try {
        const activeCount = await QueueRepository.getActiveCount()

        if (activeCount >= 10) {
            console.log("Queue Full, waiting for 5 mins")
            isInitializing = false

            setTimeout(() => {
                getRandomTrack()
            }, FULL_QUEUE_TIMEOUT)
            return
        }

        const randomTrack = await TrackRepository.getRandomTrackExcludingQueue()

        if (!randomTrack) {
            console.log('No available tracks - retrying')
            setTimeout(() => getRandomTrack(), UNIVERSAL_RETRY_TIMEOUT)
            return
        }

        console.log(`Selected random track: ${getTrackFullName(randomTrack)} (Id: ${randomTrack.id})`)

        downloadTrack(randomTrack.telegram_file_id).then(async (fileName) => {
            const coverFileName = await downloadTrackCover(randomTrack.telegram_cover_id, fileName)

            await QueueRepository.addToQueue({
                track_id: randomTrack.id,
                file_name: fileName,
                cover_file_name: coverFileName,
                status: 'pending'
            })

            console.log(`Track added to queue: ${getTrackFullName(randomTrack)}`)

            setTimeout(() => {
                getRandomTrack()
            }, isInitializing ? UNIVERSAL_RETRY_TIMEOUT : GET_RANDOM_TRACK_TIMEOUT)

        }).catch(error => {
            console.error(`Error downloading track ${getTrackFullName(randomTrack)}:`, error)
            setTimeout(() => {
                getRandomTrack()
            }, UNIVERSAL_RETRY_TIMEOUT)
        })

    } catch (error) {
        console.error('Error in getRandomTrack:', error)
        setTimeout(() => {
            getRandomTrack()
        }, UNIVERSAL_RETRY_TIMEOUT)
    }
}

exports.run = async () => {

    connect().then(async () => {
        await getTracksFromChannel(process.env.CHANNEL)

        getRandomTrack()
    })

    deleteUnusedTracks()

}

exports.getCover = async (trackId) => {
    if (!trackId) return
    let track = await QueueRepository.getTrackById(trackId)
    return track[0].cover_file_name
}
