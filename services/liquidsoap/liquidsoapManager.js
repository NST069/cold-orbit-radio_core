const fs = require("fs")
const path = require("path")
const { spawn } = require("child_process")
const os = require("os")

require('dotenv').config({ path: path.resolve(__dirname, "../../.env") })

const LIQUIDSOAP_SCRIPT_DIR = os.platform() === "linux" ? "/etc/liquidsoap" : path.join(__dirname, "liquidsoap")

const generateLiquidSoapScript = () => {
    let script = fs.readFileSync("./templates/radio_template.liq").toString()
        .replace("{PWD}", process.env.LIQUIDSOAP_PWD)
        .replace("{LOG}", __dirname.replaceAll("\\", "/") + "/logs/liquidsoap.log")
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

exports.init = async()=>{
    generateLiquidSoapScript()
    await startLiquidSoap()
}
