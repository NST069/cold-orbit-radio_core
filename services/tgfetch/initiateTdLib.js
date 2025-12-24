// Initial tdlib connection. Run on host to send OTP and 2FA
const fs = require("fs")
const path = require('path')

require('dotenv').config({ path: path.resolve(__dirname, "../../.env") })

const tdl = require('tdl')
const { getTdjson } = require('prebuilt-tdlib')
tdl.configure({ tdjson: getTdjson() })

const client = tdl.createClient({
    apiId: parseInt(process.env.API_ID),
    apiHash: process.env.API_HASH,
})

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
}

connect()
