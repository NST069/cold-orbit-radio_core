const express = require('express')
const child_process = require('child_process')
const app = express()
const { run } = require("./tracks.js")
const { init } = require("./streamManager.js")

require('dotenv').config()

const PORT = 3000

run().then(() => init())

app.listen(PORT, () => console.log(`My server is running on port ${PORT}`))