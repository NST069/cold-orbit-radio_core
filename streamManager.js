const fs = require("fs")
const path = require("path")
const { spawn } = require("child_process")
const { sendCommand, waitForTelnet } = require("./util/LiquidSoapClient")
const { markTrackAsPlayed, getQueueLength, getNextTrack, scheduleTrack, getTrackTitle } = require("./tracks")

require('dotenv').config()

const os = require("os")

const LIQUIDSOAP_SCRIPT_DIR = os.platform() === "linux" ? "/etc/liquidsoap" : path.join(__dirname, "liquidsoap")
const MUSIC_DIRECTORY = path.join(__dirname, "_td_files", "music")

let currentTrack = ""

const generateLiquidSoapScript = () => {
    let script = fs.readFileSync("./liquidsoap/radio_template.liq").toString()
        .replace("{PWD}", process.env.LIQUIDSOAP_PWD)
        .replace("{LOG}", __dirname.replaceAll("\\", "/") + "/liquidsoap/liquidsoap.log")
        .trim()

    try {
        if (!fs.existsSync(LIQUIDSOAP_SCRIPT_DIR)) {
            fs.mkdirSync(LIQUIDSOAP_SCRIPT_DIR, { recursive: true })
        }
    } catch (e) {
        console.error('[System] Failed to ensure Liquidsoap script dir:', e.message)
    }

    fs.writeFileSync(path.join(LIQUIDSOAP_SCRIPT_DIR, "radio.liq"), script, 'utf-8')
    console.log('[System] Liquidsoap script generated')
}

const startLiquidSoap = () => {
    console.log("[System] Starting Liquidsoap...")

    const scriptPath = path.join(LIQUIDSOAP_SCRIPT_DIR, "radio.liq")
    const child = spawn("liquidsoap", [scriptPath], {
        stdio: ["ignore", "pipe", "pipe"],
    })

    child.on('error', (err) => {
        console.error('[Liquidsoap spawn error]', err && err.message ? err.message : err)
    })

    child.stdout.on("data", (data) => {
        console.log(`[Liquidsoap log] ${data.toString().trim()}`);
    })

    child.stderr.on("data", (data) => {
        console.error(`[Liquidsoap error] ${data.toString().trim()}`);
    })

    child.on("exit", (code) => {
        console.log(`[Liquidsoap] exited with code ${code}`);
    })

    return child;
}

const pushTrackToLiquidSoap = async (filename) => {
    const fullPath = path.join(MUSIC_DIRECTORY, filename)
    const liquidsoapPath = fullPath.replace(/\\\\/g, "/").replace(/\\/g, "/")
    await sendCommand(`coldorbit.push ${liquidsoapPath}`)
    scheduleTrack(filename)
}

const checkTrack = async () => {
    if (getQueueLength() > 0) {
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
            console.log(getTrackTitle(trackNow))
            console.log(`[Liquidsoap] Last Check: ${currentTrack}`)
            if (currentTrack && trackNow !== currentTrack) {
                markTrackAsPlayed(currentTrack)
                pushTrackToLiquidSoap(getNextTrack()?.fileName)
            }
            currentTrack = trackNow
        }
        else pushTrackToLiquidSoap(getNextTrack()?.fileName)
    }
    setTimeout(() => {
        checkTrack()
    }, ((getQueueLength() > 0) ? 60 : 5) * 1000)
}

exports.init = async () => {
    generateLiquidSoapScript()
    await startLiquidSoap()

    // Wait for Liquidsoap telnet port to become available before starting the poller.
    const telnetReady = await waitForTelnet('127.0.0.1', 1234, 15000)
    if (!telnetReady) console.error('[System] Liquidsoap telnet port did not open within timeout (127.0.0.1:1234)')

    // If we have a local persisted queue, and Liquidsoap currently has no queued requests,
    // push the next local track immediately so playback resumes without waiting for the poller.
    try {
        if (getQueueLength() > 0) {
            const remoteLenRaw = await sendCommand('coldorbit.length').catch(e => { console.log(e); return null })
            let remoteLen = 0
            if (typeof remoteLenRaw === 'string' && remoteLenRaw.trim().length) {
                const parsed = parseInt(remoteLenRaw.trim().replace(/[^0-9]/g, ''), 10)
                remoteLen = Number.isFinite(parsed) ? parsed : 0
            } else if (typeof remoteLenRaw === 'number') {
                remoteLen = remoteLenRaw
            }
            if (remoteLen === 0) {
                const next = getNextTrack()
                if (next && next.fileName) {
                    console.log('[System] Pushing persisted next track to Liquidsoap:', next.fileName)
                    await pushTrackToLiquidSoap(next.fileName).catch(e => console.log(e))
                }
            }
        }
    } catch (e) {
        console.log('[System] Error while trying to push persisted queue on startup:', e)
    }

    checkTrack()

}