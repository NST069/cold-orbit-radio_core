const channels = require('../telegram/channels')

async function routes(fastify) {

    fastify.get(
        '/channels/by-username/:username',
        {
            schema: {
                tags: ['Channels'],
                summary: 'Get channel by username',
                params: {
                    type: 'object',
                    required: ['username'],
                    properties: {
                        username: {
                            type: 'string'
                        }
                    }
                },
                response: {
                    200: {
                        type: 'object',
                        properties: {
                            id: { type: 'integer' },
                            title: { type: 'string' },
                            username: { type: 'string' }
                        }
                    }
                }
            }
        },
        async (request, reply) => {

            try {
                const { username } = request.params

                const channel =
                    await channels.getChannelByUsername(username)

                return channel

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
