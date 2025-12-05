const express = require('express')
const app = express()

const { init, getNowPlaying } = require("./streamManager")

const PORT = 4002

init()

app.get("/health", (req, res) =>
    res.json({ status: "ok" }))

app.get("/nowPlaying", async (req, res) => {
    track = await getNowPlaying()
    res.json({ track })
})

app.on('unhandledRejection', (err) => console.error('unhandledRejection', err))
app.on('uncaughtException', (err) => console.error('uncaughtException', err))

app.listen(PORT, () => console.log(`My server is running on port ${PORT}`))
