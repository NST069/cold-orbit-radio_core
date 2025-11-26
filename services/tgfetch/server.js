const express = require('express')
const app = express()
app.use(express.json())

const { run } = require("./tracks")

const PORT = 4001

run().then(() => console.log("Ready"))

app.get("/health", (req, res) =>
    res.json({ status: "ok" }))

app.on('unhandledRejection', (err) => console.error('unhandledRejection', err))
app.on('uncaughtException', (err) => console.error('uncaughtException', err))

app.listen(PORT, () => console.log(`My server is running on port ${PORT}`))
