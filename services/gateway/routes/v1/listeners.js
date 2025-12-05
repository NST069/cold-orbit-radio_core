const express = require("express")
const router = express.Router()

const { getListenersNow } = require("../../controllers/listenersController")

/**
 * @swagger
 * /listeners:
 *   get:
 *     summary: Get Listeners Count
 *     description: Returns Listeners at the moment
 *     tags: [System]
 *     responses:
 *       200:
 *         description: Listeners Count
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 current:
 *                   type: integer
 *                   example: 150
 *                 peak_today:
 *                   type: integer
 *                   example: 320
 */
router.get("/", getListenersNow)

module.exports = router
