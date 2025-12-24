const express = require("express")
const router = express.Router()

const { getCover } = require("../../controllers/coverController")

/**
 * @swagger
 * /cover/{trackId}:
 *   get:
 *     summary: Getting Track Cover
 *     description: Getting Track Cover found in metadata by TrackId
 *     tags: [Track]
 *     parameters:
 *       - in: path
 *         name: trackId
 *         required: true
 *         schema:
 *           type: string
 *         description: Track Id
 *     responses:
 *       200:
 *         description: Track Cover
 *         content:
 *           image/jpeg:
 *             schema:
 *               type: string
 *               format: binary
 *       404 Cover Not Found:
 *         description: Track has no cover
 *       404 File Not Found:
 *         description: Cannot find specified file in the filesystem
 */
router.get("/:trackId", getCover)

module.exports = router
