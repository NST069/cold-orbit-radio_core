const express = require("express")
const app = express()

const PORT = 3000

const apiRoutes = require("./routes")
const { hateoasMiddleware } = require("./middleware/hateoas")
const swaggerSetup = require('./config/swagger')

app.use(hateoasMiddleware)
app.use("/api", apiRoutes)
app.use('/api-docs', swaggerSetup.serve, swaggerSetup.setup)

app.use((req, res) => {
    res.status(404).locals.hateoas({
        error: "Page not found",
        requested: req.originalUrl
    }, {})
})

app.use("/", (req, res) => {
    res.redirect("/api")
})

app.use((err, req, res, next) => {
    console.error(err.stack)
    res.status(500).json({ error: 'Something went wrong' })
})

app.on('unhandledRejection', (err) => console.error('unhandledRejection', err))
app.on('uncaughtException', (err) => console.error('uncaughtException', err))

app.listen(PORT, () => console.log(`My server is running on port ${PORT}`))
