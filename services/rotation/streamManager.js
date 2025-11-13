
const path = require("path")
const { sendCommand, waitForTelnet } = require("./util/LiquidSoapClient")
const { tgFetchClient } = require("./util/tgFetchClient")

require('dotenv').config({ path: path.resolve(__dirname, "../../.env") })

const MUSIC_DIRECTORY = process.env.SHARED_MUSIC_DIR || path.resolve(__dirname, "../tgfetch/_td_files", "music")

let currentTrack = ""

const pushTrackToLiquidSoap = async (filename) => {
    const fullPath = path.join(MUSIC_DIRECTORY, filename)
    const liquidsoapPath = fullPath.replace(/\\\\/g, "/").replace(/\\/g, "/")
    await sendCommand(`coldorbit.push ${liquidsoapPath}`)
    await tgFetchClient.scheduleTrack(filename)
}

const checkTrack = async () => {
    if (await tgFetchClient.getQueueLength() > 0) {
        let lengthRaw = await sendCommand("coldorbit.length").catch(e => { console.log(e); return null })
        let length = 0
        if (typeof lengthRaw === 'string' && lengthRaw.trim().length) {
            const parsed = parseInt(lengthRaw.trim().replace(/[^0-9]/g, ''), 10)
            length = Number.isFinite(parsed) ? parsed : 0
        } else if (typeof lengthRaw === 'number') {
            length = lengthRaw
        }
        console.log("length: " + length)
        if (length >= 2) {
            let trackNow = await sendCommand("coldorbit.current").then(res => {
                if (!res) return ""
                const idx = Math.max(res.lastIndexOf('/'), res.lastIndexOf('\\'))
                return res.substring(idx + 1)
            }).catch(e => console.log(e))
            console.log(`[Liquidsoap] Now Playing: ${trackNow}`)
            console.log(await tgFetchClient.getTrackTitle(trackNow))
            console.log(`[Liquidsoap] Last Check: ${currentTrack}`)
            if (currentTrack && trackNow !== currentTrack) {
                await tgFetchClient.markTrackAsPlayed(currentTrack)
                pushTrackToLiquidSoap(await tgFetchClient.getNextTrack())
            }
            currentTrack = trackNow
        }
        else pushTrackToLiquidSoap(await tgFetchClient.getNextTrack())
    }
    setTimeout(() => {
        checkTrack()
    }, ((await tgFetchClient.getQueueLength() > 0) ? 60 : 5) * 1000)
}

exports.init = async () => {

    // Wait for Liquidsoap telnet port to become available before starting the poller.
    const telnetReady = await waitForTelnet('127.0.0.1', 1234, 15000)
    if (!telnetReady) console.error('[System] Liquidsoap telnet port did not open within timeout (127.0.0.1:1234)')

    // If we have a local persisted queue, and Liquidsoap currently has no queued requests,
    // push the next local track immediately so playback resumes without waiting for the poller.
    try {
        if (await tgFetchClient.getQueueLength() > 0) {
            const remoteLenRaw = await sendCommand('coldorbit.length').catch(e => { console.log(e); return null })
            let remoteLen = 0
            if (typeof remoteLenRaw === 'string' && remoteLenRaw.trim().length) {
                const parsed = parseInt(remoteLenRaw.trim().replace(/[^0-9]/g, ''), 10)
                remoteLen = Number.isFinite(parsed) ? parsed : 0
            } else if (typeof remoteLenRaw === 'number') {
                remoteLen = remoteLenRaw
            }
            if (remoteLen === 0) {
                const next = await tgFetchClient.getNextTrack()
                if (next) {
                    console.log('[System] Pushing persisted next track to Liquidsoap:', next)
                    await pushTrackToLiquidSoap(next).catch(e => console.log(e))
                }
            }
        }
    } catch (e) {
        console.log('[System] Error while trying to push persisted queue on startup:', e)
    }

    checkTrack()

}