const { default: fastify } = require('fastify')
const fs = require('fs/promises')
const path = require('path')

const logger = require('./logger')

/**
 * FileCleaner
 * - удаляет файлы после стрима
 * - периодически чистит старые файлы
 */
class FileCleaner {
    constructor(options = {}) {
        this.dir = options.dir || path.resolve(__dirname, '_td_files')
        this.ttlMs = options.ttlMs || 60 * 60 * 1000             // 1h
        this.intervalMs = options.intervalMs || 15 * 60 * 1000   // 15m
        this.enabled = options.enabled ?? true

        this.timer = null
    }

    /**
     * START background cleanup job
     */
    start() {
        if (!this.enabled) return

        this.timer = setInterval(() => {
            this.cleanup().catch(err => {
                logger.error({
                    error: err
                }, '[FileCleaner] cleanup error')
            })
        }, this.intervalMs)

        logger.debug({
            ttl: `${this.ttlMs}ms`,
            interval: `${this.intervalMs}ms`
        }, '[FileCleaner] started')
    }

    /**
     * STOP job (graceful shutdown)
     */
    stop() {
        if (this.timer) {
            clearInterval(this.timer)
            this.timer = null
        }
    }

    /**
     * Reactive cleanup (after stream finished)
     */
    async safeDelete(filePath) {
        if (!filePath) return

        try {
            await fs.unlink(filePath)
            logger.debug(`[FileCleaner] deleted: ${filePath}`)
        } catch (err) {
            // already deleted or in use → ignore safely
            if (err.code !== 'ENOENT') {
                logger.warn({
                    error: err,
                    filePath: filePath
                }, '[FileCleaner] delete error')
            }
        }
    }

    /**
     * Full directory cleanup (TTL-based)
     */
    async cleanup() {
        const files = await fs.readdir(this.dir)
        const now = Date.now()

        for (const file of files) {
            const filePath = path.join(this.dir, file)

            try {
                const stat = await fs.stat(filePath)

                // skip directories
                if (!stat.isFile()) continue

                const age = now - stat.mtimeMs

                if (age > this.ttlMs) {
                    await fs.unlink(filePath)
                    logger.debug(`[FileCleaner] TTL removed: ${file}`)
                }
            } catch (err) {
                if (err.code !== 'ENOENT') {
                    logger.warn({
                        error: err,
                        filePath: filePath
                    }, '[FileCleaner] stat error')
                }
            }
        }
    }
}

module.exports = FileCleaner
