const services = require("../services")

exports.getNowPlaying = async (req, res) => {
    try {
        const track = await services.getNowPlaying()

        const links = {}

        if (track && track.hasCover) links.cover = {
            href: `${req.protocol}://${req.get("host")}/api/v1/cover/${track.id}`,
            method: "GET",
            type: "image/jpeg"
        }

        res.locals.hateoas({
            ...track,
            fetchedAt: new Date().toISOString()
        }, links)
    }
    catch (error) {
        console.log(error)
        res.locals.hateoas({
            status: 'OFFLINE',
            message: 'Service Unavailable'
        }, {
            retry: { href: req.originalUrl, method: 'GET' },
            status: { href: '/api/v1/health', method: 'GET' }
        })
    }
}
