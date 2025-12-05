const express = require("express")
const router = express.Router()

const { getNowPlaying } = require("../../controllers/nowPlayingController")

/**
 * @swagger
 * /now-playing:
 *   get:
 *     summary: Get detailed track information about track playing now
 *     description: Returns complete track metadata including artists, play statistics, and Telegram file information
 *     tags: [Track]
 *     responses:
 *       200:
 *         description: Track information with artists details
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 track:
 *                   type: object
 *                   properties:
 *                     id:
 *                       type: integer
 *                       example: 1
 *                       description: Internal track ID
 *                     telegram_id:
 *                       type: string
 *                       example: "987654321"
 *                       description: Telegram user/chat ID who uploaded the track
 *                     title:
 *                       type: string
 *                       example: "Never Gonna Give You Up"
 *                       description: Track title
 *                     performer:
 *                       type: string
 *                       example: "Rick Astley"
 *                       description: Performer/artist name (display version)
 *                     file_name:
 *                       type: string
 *                       example: "Rick Astley - Never Gonna Give You Up.mp3"
 *                       description: Original filename
 *                     duration:
 *                       type: integer
 *                       example: 213
 *                       description: Track duration in seconds
 *                     file_size:
 *                       type: string
 *                       example: "8650752"
 *                       description: File size in bytes
 *                     telegram_file_id:
 *                       type: string
 *                       example: "CQACAgIAAxkBAAIBjWRAAAEDgQABHqHyHwABHqHyHwABHqHyHwACqQADJGkAAUZpGQABXGk4pDgE"
 *                       description: Telegram file ID for downloading
 *                     telegram_cover_id:
 *                       type: string
 *                       nullable: true
 *                       example: "AgACAgIAAxkBAAIBjmRAAAEDhQABHqHyHwABHqHyHwABHqHyHwADFQADJGkAAUZpGQABXGk4pDgABh4AAwQ"
 *                       description: Telegram cover image file ID
 *                     caption:
 *                       type: string
 *                       example: "Rick Astley – Never Gonna Give You Up"
 *                       description: Original caption from Telegram
 *                     total_plays:
 *                       type: integer
 *                       example: 1000000
 *                       minimum: 0
 *                       description: Total number of times track was played
 *                     like_count:
 *                       type: integer
 *                       example: 50000
 *                       minimum: 0
 *                       description: Number of likes
 *                     dislike_count:
 *                       type: integer
 *                       example: 100
 *                       minimum: 0
 *                       description: Number of dislikes
 *                     created_at:
 *                       type: string
 *                       format: date-time
 *                       example: "2023-01-15T12:00:00.000Z"
 *                       description: Track creation timestamp in database
 *                     updated_at:
 *                       type: string
 *                       format: date-time
 *                       example: "2024-12-04T14:30:45.123Z"
 *                       description: Last track update timestamp
 *                     artists:
 *                       type: array
 *                       description: List of artists associated with the track
 *                       items:
 *                         type: object
 *                         properties:
 *                           id:
 *                             type: integer
 *                             example: 1
 *                           name:
 *                             type: string
 *                             example: "Rick Astley"
 *                           normalized_name:
 *                             type: string
 *                             example: "rick astley"
 *                           created_at:
 *                             type: string
 *                             format: date-time
 *                             example: "2023-01-15T12:00:00.000Z"
 */
router.get("/", getNowPlaying)

module.exports = router
