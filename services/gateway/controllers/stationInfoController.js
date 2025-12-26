const services = require("../services")

exports.getStationInfo = async (req, res) => {
    try {
        const response = await services.getStationInfo()

        const links = {
            nowPlaying: {
                href: `${req.protocol}://${req.get("host")}/api/v1/now-playing`,
                method: "GET",
                type: "application/json"
            },
            listeners: {
                href: `${req.protocol}://${req.get("host")}/api/v1/listeners`,
                method: "GET",
                type: "application/json"
            },
            health: {
                href: `${req.protocol}://${req.get("host")}/api/v1/health`,
                method: "GET",
                type: "application/json"
            }
        }

        res.locals.hateoas({
            ...response,
            fetchedAt: new Date().toISOString()
        }, links)
        return response
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
