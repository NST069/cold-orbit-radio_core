
const path = require("path")
const { sendCommand, waitForTelnet } = require("./util/LiquidSoapClient")

require('dotenv').config({ path: path.resolve(__dirname, "../../.env") })

const TrackRepository = require(path.resolve(process.env.SHARED_DB_DIR, "repositories/TrackRepository"))
const QueueRepository = require(path.resolve(process.env.SHARED_DB_DIR, "repositories/QueueRepository"))

const MUSIC_DIRECTORY = process.env.SHARED_MUSIC_DIR || path.resolve(__dirname, "../tgfetch/_td_files", "music")

let currentTrack = ""

const CHECK_TRACK_TIMEOUT = 60 * 1000                       //1 min
const CHECK_TRACK_TIMEOUT_SHORT = 5 * 1000                  //5 sec
const UNIVERSAL_RETRY_TIMEOUT = 30 * 1000                   //30 sec

const pushTrackToLiquidSoap = async (queueItem) => {
    try {
        const fullPath = path.join(MUSIC_DIRECTORY, queueItem.file_name)
        const liquidsoapPath = fullPath.replace(/\\\\/g, "/").replace(/\\/g, "/")
        await sendCommand(`coldorbit.push ${liquidsoapPath}`)
        await QueueRepository.updateQueueStatus(queueItem.file_name, "scheduled")
    } catch (e) {
        console.error(`Error pushing track to Liquidsoap: ${queueItem.file_name}`, e)
        await QueueRepository.updateQueueStatus(queueItem.file_name, "failed")
    }

}

const checkTrack = async () => {
    try {
        const queueLength = await QueueRepository.getActiveCount()
        if (queueLength > 0) {
            let lengthRaw = await sendCommand("coldorbit.length").catch(e => {
                console.log(e)
                return null
            })
            let length = 0
            if (typeof lengthRaw === 'string' && lengthRaw.trim().length) {
                const parsed = parseInt(lengthRaw.trim().replace(/[^0-9]/g, ''), 10)
                length = Number.isFinite(parsed) ? parsed : 0
            } else if (typeof lengthRaw === 'number') {
                length = lengthRaw
            }
            console.log("Liquidsoap queue length: " + length)
            if (length >= 2) {
                let trackNow = await sendCommand("coldorbit.current").then(res => {
                    if (!res) return ""
                    const idx = Math.max(res.lastIndexOf('/'), res.lastIndexOf('\\'))
                    return res.substring(idx + 1)
                }).catch(e => console.log(e))
                console.log(`[Liquidsoap] Now Playing: ${trackNow}`)

                console.log(`[Liquidsoap] Last Check: ${currentTrack}`)
                if (currentTrack && trackNow !== currentTrack) {
                    await QueueRepository.updateQueueStatus(currentTrack, "played")
                    const nextTrack = await QueueRepository.getNextTrack()
                    if (!nextTrack) throw new Error("nextTrack is null")
                    pushTrackToLiquidSoap(nextTrack)
                }
                currentTrack = trackNow || ""
            }
            else {
                const nextTrack = await QueueRepository.getNextTrack()
                if (!nextTrack) throw new Error("nextTrack is null")
                pushTrackToLiquidSoap(nextTrack)
            }
        }
        else console.log("Queue is empty. Waiting...")
        setTimeout(() => {
            checkTrack()
        }, (queueLength > 0) ? CHECK_TRACK_TIMEOUT : CHECK_TRACK_TIMEOUT_SHORT)
    } catch (e) {
        console.error("Failed to check track, Retrying...", e)
        setTimeout(() => {
            checkTrack()
        }, UNIVERSAL_RETRY_TIMEOUT)
    }
}

exports.init = async () => {

    // Wait for Liquidsoap telnet port to become available before starting the poller.
    const telnetReady = await waitForTelnet('127.0.0.1', 1234, 15000)
    if (!telnetReady) console.error('[System] Liquidsoap telnet port did not open within timeout (127.0.0.1:1234)')

    // If we have a local persisted queue, and Liquidsoap currently has no queued requests,
    // push the next local track immediately so playback resumes without waiting for the poller.
    try {
        const queueLength = await QueueRepository.getActiveCount()
        if (queueLength > 0) {
            const remoteLenRaw = await sendCommand('coldorbit.length').catch(e => {
                console.log(e)
                return null
            })
            let remoteLen = 0
            if (typeof remoteLenRaw === 'string' && remoteLenRaw.trim().length) {
                const parsed = parseInt(remoteLenRaw.trim().replace(/[^0-9]/g, ''), 10)
                remoteLen = Number.isFinite(parsed) ? parsed : 0
            } else if (typeof remoteLenRaw === 'number') {
                remoteLen = remoteLenRaw
            }
            if (remoteLen === 0) {
                await QueueRepository.clearScheduled()
                const nextTrack = await QueueRepository.getNextTrack()
                if (nextTrack) {
                    console.log('[System] Pushing persisted next track to Liquidsoap:', nextTrack.file_name)
                    await pushTrackToLiquidSoap(nextTrack).catch(e => console.log(e))
                }
            }
        }
    } catch (e) {
        console.log('[System] Error while trying to push persisted queue on startup:', e)
    }

    checkTrack()

}

exports.getNowPlaying = async () => {

    let trackFileName = await sendCommand("coldorbit.current").then(res => {
        if (!res) return ""
        const idx = Math.max(res.lastIndexOf('/'), res.lastIndexOf('\\'))
        return res.substring(idx + 1)
    }).catch(e => console.log(e))

    return await TrackRepository.findByQueueFileName(trackFileName)
}
