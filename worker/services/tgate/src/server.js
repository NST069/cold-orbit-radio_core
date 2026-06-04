require('dotenv').config()

const Fastify = require('fastify')
const path = require('path')

const runtime = require('./telegram/runtime')

const logger = require('./services/logger')

const channelRoutes = require('./routes/channels')
const messageRoutes = require('./routes/messages')
const fileRoutes = require('./routes/files')
const health = require('./routes/health')

const FileCleaner = require('./services/fileCleaner')
const { default: fastify } = require('fastify')

const app = Fastify({
    loggerInstance: logger
})

const cleaner = new FileCleaner({
    dir: path.resolve('/telegram-session/files', 'music'),
    ttlMs: 60 * 60 * 1000,        // 1h
    intervalMs: 15 * 60 * 1000,    // 15 min
})

cleaner.start()

process.on('SIGTERM', async () => {
    cleaner.stop()

    await app.close()
})

app.register(
    require('@fastify/swagger'),
    {
        openapi: {
            info: {
                title: 'Telegram Service',
                description: 'TDLib wrapper service',
                version: '1.0.0'
            }
        }
    }
)

app.register(
    require('@fastify/swagger-ui'),
    {
        routePrefix: '/docs'
    }
)

app.register(channelRoutes)
app.register(messageRoutes)
app.register(fileRoutes, { cleaner })
app.register(health)

app.get('/me', async () => {
    return runtime.getCurrentUser()
})

async function bootstrap() {
    try {
        await runtime.start()

        const port = Number(process.env.PORT || 3000)

        await app.listen({
            port,
            host: '0.0.0.0'
        })

        logger.info(`[SERVER] Started on port ${port}`)

    } catch (err) {
        app.log.error(err)
        logger.error(err)
        process.exit(1)
    }
}

bootstrap()
