const runtime = require('../telegram/runtime')

async function routes(fastify) {

    fastify.get('/on', async () => {
        return { status: 'ok' }
    })

    fastify.get(
        '/health',
        {
            schema: {
                tags: ['System'],
                summary: 'Health check',
                response: {
                    200: {
                        type: 'object',
                        properties: {
                            status: { type: 'string', default: 'UP' },
                            telegram: { type: 'string', default: 'ready' },
                            uptimeSeconds: { type: 'integer' }
                        }
                    }
                }
            }
        },
        async (request, reply) => {
            return {
                status: 'UP',
                telegram: runtime.isReady()
                    ? 'ready'
                    : 'starting',
                uptimeSeconds: Math.floor(process.uptime())
            }
        }
    )
}

module.exports = routes