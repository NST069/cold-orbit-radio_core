const express = require('express')
const app = express()

const { init } = require("./liquidsoapManager");

const PORT = 4000

init().then(() => console.log("Ready"))

app.on('unhandledRejection', (err) => console.error('unhandledRejection', err));
app.on('uncaughtException', (err) => console.error('uncaughtException', err));

app.listen(PORT, () => console.log(`My server is running on port ${PORT}`))