const services = require("../services")

const path = require('path')
const fs = require('fs').promises
const mime = require('mime-types')

const SHARED_PATH = process.env.SHARED_PATH || '/shared/music'

exports.getCover = async (req, res) => {
    try {
        trackId = req.params.trackId
        coverInfo = await services.getTrackCover(trackId)

        if (!coverInfo || !coverInfo.fileName) {
            return res.status(404).hateoas({
                error: 'Cover Not Found',
                trackId
            })
        }

        coverFilePath = path.join(SHARED_PATH, coverInfo.fileName)

        try {
            await fs.access(coverFilePath)
        }
        catch {
            return res.status(404).hateoas({ error: 'File Not Found' })
        }

        const mimeType = mime.lookup(coverFilePath) || 'application/octet-stream'

        res.setHeader('Content-Type', mimeType)
        res.setHeader('Cache-Control', 'public, max-age=86400')

        res.sendFile(coverFilePath)
    } catch (error) {
        console.error('Error sending file:', error)
        res.status(500).hateoas({ error: 'Error sending file' })
    }
}
