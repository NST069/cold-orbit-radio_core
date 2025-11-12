const express = require('express')
const app = express()

const { init } = require("./streamManager")

const PORT = 4002

init()

app.listen(PORT, () => console.log(`My server is running on port ${PORT}`))