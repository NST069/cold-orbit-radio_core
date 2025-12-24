const express = require("express")
const router = express.Router()

const { getHealth } = require("../../controllers/healthController")

/**
 * @swagger
 * /health:
 *   get:
 *     summary: Service Health check
 *     description: Checks if all Services running
 *     tags: [System]
 *     responses:
 *       200:
 *         description: All systems working
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               description: For All Services
 *               properties:
 *                 service_name:
 *                   type: array
 *                   description: Service status information
 *                   items:
 *                   status:
 *                     type: string
 *                     enum: [OK, WARNING, ERROR]
 *                     example: "OK"
 *                   url:
 *                     type: string
 *                     example: "localhost:3000"
 */
router.get("/", getHealth)

module.exports = router
