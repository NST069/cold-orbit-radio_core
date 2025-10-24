const fs = require("fs")
const { spawn } = require("child_process")
const { sendCommand } = require("./util/LiquidsoapClient")
const { removeTrackFromQueue, getQueueLength, getNextTrack, scheduleTrack } = require("./tracks")

require('dotenv').config()

const os = require("os")

const LIQUIDSOAP_SCRIPT_DIR = os.platform() == "linux" ? "\\etc\\liquidsoap" : ".\\liquidsoap"
const MUSIC_DIRECTORY = __dirname + "\\_td_files\\music\\"

let currentTrack = ""

generateLiquidSoapScript = () => {
    let script = fs.readFileSync("./liquidsoap/radio_template.liq").toString()
        .replace("{PWD}", process.env.LIQUIDSOAP_PWD)
        .replace("{LOG}", __dirname.replaceAll("\\", "/") + "/liquidsoap/liquidsoap.log")
        .replace("{ROT}", __dirname.replaceAll("\\", "/") + "/rot/")
        .trim()

    fs.writeFileSync(LIQUIDSOAP_SCRIPT_DIR + "\\radio.liq", script, 'utf-8')
    console.log('[System] Liquidsoap скрипт сгенерирован')
}

startLiquidSoap = () => {
    console.log("[System] Starting Liquidsoap...")

    const child = spawn("liquidsoap", [LIQUIDSOAP_SCRIPT_DIR + "\\radio.liq"], {
        stdio: ["ignore", "pipe", "pipe"],
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

pushTrackToLiquidSoap = async (filename) => {
    await sendCommand(`coldorbit.push ${MUSIC_DIRECTORY + filename}`)
    scheduleTrack(filename)
}

checkTrack = async () => {
    if (getQueueLength() > 0) {
        let length = await sendCommand("coldorbit.length").catch(e => console.log(e))
        console.log("length: " + length)
        if (length >= 2) {
            let trackNow = await sendCommand("coldorbit.current").catch(e => console.log(e))
            console.log(`Now Playing: ${trackNow}`)
            console.log(`Last Check: ${currentTrack}`)
            if (currentTrack && trackNow !== currentTrack) {
                removeTrackFromQueue()
                pushTrackToLiquidSoap(getNextTrack()?.fileName)
            }
            currentTrack = trackNow
        }
        else pushTrackToLiquidSoap(getNextTrack().fileName)
    }
    setTimeout(() => {
        checkTrack()
    }, ((getQueueLength() > 0) ? 60 : 5) * 1000)
}

exports.init = async () => {
    generateLiquidSoapScript()
    await startLiquidSoap()

    checkTrack()

}