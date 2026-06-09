const client = require('./client')
const { mapMessage } = require('./mapper')

async function getMessages(chatId, limit, cursor) {

    await client.invoke({
        '@type': 'openChat',
        chat_id: chatId
    })

    const history = await client.invoke({
        '@type': 'getChatHistory',
        chat_id: chatId,
        from_message_id: cursor || 0,
        limit,
        offset: 0
    })

    const items = history.messages.map(mapMessage)

    const last = history.messages[history.messages.length - 1]

    return {
        items,
        nextCursor: last?.id || null,
        hasMore: history.messages.length === limit
    }
}

module.exports = {
    getMessages
}
