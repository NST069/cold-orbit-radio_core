const client = require('./client')

async function getChannelByUsername(username) {
    const normalized = username.replace('@', '')

    const channel = await client.invoke({
        '@type': 'searchPublicChat',
        username: normalized
    })

    return {
        id: channel.id,
        title: channel.title,
        username: normalized,
        type: channel.type?.['@type'] || null
    }
}

module.exports = {
    getChannelByUsername
}
