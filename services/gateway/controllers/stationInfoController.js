const services = require("../services")

exports.getStationInfo = async (req, res) => {
    try {
        const response = await services.getStationInfo()

        const links = {}

        res.hateoas({
            ...response,
            fetchedAt: new Date().toISOString()
        }, links)
        return response
    }
    catch (error) {
        console.log(error)
        res.hateoas({
            status: 'OFFLINE',
            message: 'Service Unavailable'
        }, {
            retry: { href: req.originalUrl, method: 'GET' },
            status: { href: '/api/v1/health', method: 'GET' }
        });
    }
}
