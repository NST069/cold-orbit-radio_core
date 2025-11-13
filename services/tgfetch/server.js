const express = require('express')
const app = express()
app.use(express.json())

const { run, getNextTrack, getQueueLength, getTrackTitle, scheduleTrack, markTrackAsPlayed } = require("./tracks")

const PORT = 4001

run().then(() => console.log("Ready"))

app.get("/health", (req, res) =>
    res.json({ status: "ok" }))

app.get("/api/next-track", (req, res) =>
    res.status(200).json({ data: getNextTrack().fileName }))

app.get("/api/queue-length", async (req, res) => 
    res.status(200).json({ data: await getQueueLength() }))

app.get("/api/track-title", (req, res) =>
    res.status(200).json({ data: getTrackTitle(req.query.fileName) }))

app.post("/api/mark-scheduled", (req, res) =>
    res.status(200).send(`scheduled: ${scheduleTrack(req.body.fileName)}`))

app.post("/api/mark-played", (req, res) =>
    res.status(200).send(`played: ${markTrackAsPlayed(req.body.fileName)}`))

app.on('unhandledRejection', (err) => console.error('unhandledRejection', err));
app.on('uncaughtException', (err) => console.error('uncaughtException', err));

app.listen(PORT, () => console.log(`My server is running on port ${PORT}`))
