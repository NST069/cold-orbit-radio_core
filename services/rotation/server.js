const express = require('express')
const app = express()

const { init, getNowPlaying, changeTrackQueueStatusExternal } = require("./streamManager")

app.use(express.urlencoded({ extended: true }))
app.use(express.json())

const PORT = 4002

init()

app.get("/health", (req, res) =>
    res.json({ status: "ok" }))

app.get("/nowPlaying", async (req, res) => {
    let track = await getNowPlaying()
    res.json(track)
})

app.post('/api/liquidsoap/webhook', async (req, res) => {
    const { event, artist, title, album, duration, timestamp } = req.body

    console.log(`Processing webhook: ${event} - ${artist} - ${title}`)

    if (event === 'track_start') {
        changeTrackQueueStatusExternal(artist.trim(), title.trim(), 'playing')

    } else if (event === 'track_end') {
        changeTrackQueueStatusExternal(artist.trim(), title.trim(), 'played')
    }

    res.status(200).send('OK')
})

app.on('unhandledRejection', (err) => console.error('unhandledRejection', err))
app.on('uncaughtException', (err) => console.error('uncaughtException', err))

app.listen(PORT, () => console.log(`My server is running on port ${PORT}`))
