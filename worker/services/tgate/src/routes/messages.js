const messages = require('../telegram/messages')

async function routes(fastify) {

    fastify.get(
        '/messages/:chatId',
        {
            schema: {
                tags: ['Messages'],
                summary: 'Get chat messages',
                params: {
                    type: 'object',
                    required: ['chatId'],
                    properties: {
                        chatId: {
                            type: 'integer'
                        }
                    }
                },
                querystring: {
                    type: 'object',
                    properties: {
                        limit: {
                            type: 'integer',
                            default: 100
                        },
                        fromMessageId: {
                            type: 'integer'
                        }
                    }
                }
            },
            response: {
                200: {
                    type: 'array',
                    items: {
                        type: 'object',
                        properties: {
                            id: { type: 'integer' },
                            chatId: { type: 'integer' },
                            date: { type: 'integer' },
                            type: { type: 'string' },
                            caption: { type: 'string' }
                        }
                    }
                }
            }
        },
        async (request, reply) => {

            try {

                const { chatId } = request.params

                const limit =
                    Number(request.query.limit || 100)

                const cursor =
                    Number(request.query.cursor || 0)

                return {
                    messages:
                        await messages.getMessages(
                            chatId,
                            limit,
                            cursor
                        )
                }

            } catch (err) {

                reply.code(500)

                return {
                    error: err.message
                }
            }
        }
    )
}

module.exports = routes
