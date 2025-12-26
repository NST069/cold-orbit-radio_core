const express = require("express")
const router = express.Router()

const nowPlayingRouter = require("./now-playing")
const healthRouter = require("./health")
const listenersRouter = require("./listeners")
const coverRouter = require("./cover")

router.use("/now-playing", nowPlayingRouter)
router.use("/health", healthRouter)
router.use("/listeners", listenersRouter)
router.use("/cover", coverRouter)

const { getStationInfo } = require("../../controllers/stationInfoController")

/**
 * @swagger
 * /:
 *   get:
 *     summary: Get radio station status and current stream info
 *     description: Returns complete information about the radio station including current song, listeners count, stream metadata and now playing status
 *     tags: [System]
 *     responses:
 *       200:
 *         description: Radio station status information
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 name:
 *                   type: string
 *                   example: "Cold Orbit Radio"
 *                   description: Radio station name
 *                 description:
 *                   type: string
 *                   example: "Interplanetary broadcast station"
 *                   description: Station description
 *                 genre:
 *                   type: string
 *                   example: "Various"
 *                   description: Music genre(s)
 *                 url:
 *                   type: string
 *                   format: uri
 *                   example: "http://localhost:8000/radio.mp3"
 *                   description: Stream URL
 *                 currentSong:
 *                   type: string
 *                   example: "No song playing"
 *                   description: Current playing song title
 *                 listeners:
 *                   type: integer
 *                   example: 0
 *                   minimum: 0
 *                   description: Current number of active listeners
 *                 peakListeners:
 *                   type: integer
 *                   example: 0
 *                   minimum: 0
 *                   description: Peak listeners count for current stream session
 *                 streamStart:
 *                   type: string
 *                   format: date-time
 *                   example: "Thu, 04 Dec 2025 16:39:59 +0000"
 *                   description: Stream start timestamp (RFC 2822 format)
 */
router.get("/", getStationInfo)

module.exports = router
