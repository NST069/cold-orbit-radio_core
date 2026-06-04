const tdl = require('tdl')
const { getTdjson } = require('prebuilt-tdlib')

tdl.configure({
    tdjson: getTdjson(),
})

const client = tdl.createClient({
    apiId: Number(process.env.TELEGRAM_API_ID),
    apiHash: process.env.TELEGRAM_API_HASH,

    databaseDirectory: '/telegram-session/database',
    filesDirectory: '/telegram-session/files'
})

module.exports = client
