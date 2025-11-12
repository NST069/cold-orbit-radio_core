const express = require('express')
const app = express()
app.use(express.json())

const { run, getNextTrack, getQueueLength, getTrackTitle, scheduleTrack, markTrackAsPlayed } = require("./tracks")

const PORT = 4001

run().then(() => console.log("Ready"))

app.get("/health", (req, res) =>
    res.json({ status: "ok" }))

app.get("/api/next-track", (req, res) =>
    res.json({ data: getNextTrack().fileName }))

app.get("/api/queue-length", async (req, res) => {
    try {
        console.log("called "+await getQueueLength())
        res.status(200).json({ data: await getQueueLength() })//)
    } catch (err) {
        console.error("❌ Error in /mark-scheduled:", err);
        res.status(500).json({ error: err.message });
    }
})

app.get("/api/track-title", (req, res) =>
    res.json({ data: getTrackTitle(req.query.fileName) }))

app.post("/api/mark-scheduled", (req, res) =>
    res.send(`scheduled: ${scheduleTrack(req.body.fileName)}`))

app.post("/api/mark-played", (req, res) =>
    res.send(`played: ${markTrackAsPlayed(req.body.fileName)}`))

app.use((err, req, res, next) => {
  console.error("Express error:", err);
  res.status(500).json({ error: err.message });
})

app.listen(PORT, () => console.log(`My server is running on port ${PORT}`))
