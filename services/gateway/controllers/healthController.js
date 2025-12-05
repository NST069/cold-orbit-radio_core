const services = require("../services")

exports.getHealth = async (req, res) => {
    try {
        const response = await services.checkAllServices()

        const links = {}

        res.hateoas({
            ...response,
            fetchedAt: new Date().toISOString()
        }, links)
    }
    catch (error) {
        res.hateoas({
            status: 'OFFLINE',
            message: 'Service Unavailable'
        }, {
            retry: { href: req.originalUrl, method: 'GET' },
            status: { href: '/api/v1/health', method: 'GET' }
        });
    }
}
