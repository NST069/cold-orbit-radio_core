const express = require('express')
const app = express()

const { init } = require("./streamManager")

const PORT = 4002

init()

app.on('unhandledRejection', (err) => console.error('unhandledRejection', err));
app.on('uncaughtException', (err) => console.error('uncaughtException', err));

app.listen(PORT, () => console.log(`My server is running on port ${PORT}`))