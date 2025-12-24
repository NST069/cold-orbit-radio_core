const fs = require("fs")
const path = require("path")
const { spawn } = require("child_process")
const os = require("os")

require('dotenv').config({ path: path.resolve(__dirname, "../../.env") })

const LIQUIDSOAP_SCRIPT_PATH = process.env.LIQUIDSOAP_SCRIPT_PATH

const generateLiquidSoapScript = async () => {
    let script = fs.readFileSync("./templates/radio_template.liq").toString()
        .replace(/{{ICECAST_HOST}}/g, process.env.ICECAST_HOST || 'localhost')
        .replace(/{{ICECAST_PORT}}/g, process.env.ICECAST_PORT || '8000')
        .replace(/{{ICECAST_PASSWORD}}/g, process.env.ICECAST_PASSWORD || 'hackme')
        .replace(/{{ICECAST_MOUNT}}/g, process.env.ICECAST_MOUNT || 'radio.mp3')
        .replace(/{{LIBREFM_USER}}/g, process.env.LIBREFM_USER || 'dummy')
        .replace(/{{LIBREFM_PASSWORD}}/g, process.env.LIBREFM_PASSWORD || 'hackme')
        .replace(/{{LOG}}/g, path.join(LIQUIDSOAP_SCRIPT_PATH, "logs/liquidsoap.log"))
        .replace(/{{CODEC}}/g, (os.platform() === "win32") ? "%ffmpeg(format=\"mp3\", %audio(codec=\"libmp3lame\"))" : "%mp3")
        .replace(/{{WEBHOOK_URL}}/g, process.env.WEBHOOK_URL || '')
        .trim()

    try {
        if (!fs.existsSync(LIQUIDSOAP_SCRIPT_PATH)) {
            fs.mkdirSync(LIQUIDSOAP_SCRIPT_PATH, { recursive: true })
            fs.mkdirSync(path.join(LIQUIDSOAP_SCRIPT_PATH, "logs"), { recursive: true })
        }
    } catch (e) {
        console.error('[System] Failed to ensure Liquidsoap script dir:', e.message)
    }

    fs.writeFileSync(path.join(LIQUIDSOAP_SCRIPT_PATH, "radio_new.liq"), script, 'utf-8')
    console.log('[System] Liquidsoap script generated')

    try {
        const result = await validateScript(path.join(LIQUIDSOAP_SCRIPT_PATH, "radio_new.liq"))
        console.log('[Validation] OK:', result.stdout)
        restartLiquidsoap(path.join(LIQUIDSOAP_SCRIPT_PATH, "radio_new.liq"))
    } catch (error) {
        console.error('[Validation] Failed:', error.message)
    }
}

const validateScript = async (scriptPath) => {
    return new Promise((resolve, reject) => {
        const child = spawn('liquidsoap', ['--check', scriptPath], {
            stdio: ['ignore', 'pipe', 'pipe']
        })

        let stdout = ''
        let stderr = ''

        child.stdout.on('data', (data) => {
            stdout += data.toString()
        })

        child.stderr.on('data', (data) => {
            stderr += data.toString()
        })

        child.on('close', (code) => {
            if (code === 0) {
                resolve({
                    valid: true,
                    stdout: stdout.trim(),
                    stderr: stderr.trim()
                });
            } else {
                reject(new Error(`Syntax error (code ${code}):\n${stderr.trim()}`))
            }
        })

        child.on('error', (err) => {
            reject(new Error(`Spawn error: ${err.message}`))
        })
    })
}

const restartLiquidsoap = (scriptPath) => {
    return new Promise((resolve, reject) => {
        const child = spawn('./checkRestart.sh', [scriptPath], {
            stdio: ['ignore', 'pipe', 'pipe']
        })

        let stdout = ''
        let stderr = ''

        child.on('error', (err) => {
            console.error('[Liquidsoap Restarter spawn error]', err && err.message ? err.message : err)
            reject(err)
        })

        child.stdout.on("data", (data) => {
            stdout += data.toString()
            console.log(`[Liquidsoap Restarter log] ${data.toString().trim()}`)
        })

        child.stderr.on("data", (data) => {
            stderr += data.toString()
            console.error(`[Liquidsoap Restarter error] ${data.toString().trim()}`)
        })

        child.on("exit", (code) => {
            console.log(`[Liquidsoap Restarter] exited with code ${code}`)
            if (code === 0) {
                resolve({ success: true, log: stdout });
            } else {
                reject(new Error(`Script failed with code ${code}: ${stderr}`));
            }
        })
    })
}

exports.init = async () => {
    await generateLiquidSoapScript()
}
