const client = require('./client')

const logger = require('../services/logger')

let ready = false
let me = null

async function start() {
    client.on('error', (err) => {
        logger.error('[TDLIB ERROR]', err)
    })

    me = await client.invoke({
        '@type': 'getMe'
    })

    ready = true

    logger.info({
        firstName: me.first_name,
        id: me.id
    },
        '[TDLIB] Authorized')
}

function isReady() {
    return ready
}

function getCurrentUser() {
    return me
}

module.exports = {
    start,
    isReady,
    getCurrentUser,
}
