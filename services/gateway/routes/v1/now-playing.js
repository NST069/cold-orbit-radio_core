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
 *                 id:
 *                   type: integer
 *                   example: 1
 *                   description: Track Id
 *                 title:
 *                   type: string
 *                   example: "Never Gonna Give You Up"
 *                   description: Track title
 *                 performer:
 *                    type: string
 *                    example: "Rick Astley"
 *                    description: Performer/artist name (display version)
 *                 duration:
 *                    type: integer
 *                    example: 213
 *                    description: Track duration in seconds
 *                 hasCover:
 *                    type: boolean
 *                    example: true
 *                    description: Track hac cover image
 */
router.get("/", getNowPlaying)

module.exports = router
