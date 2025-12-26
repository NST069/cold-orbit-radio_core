const services = require("../services")

exports.getHealth = async (req, res) => {
    try {
        const response = await services.checkAllServices()

        const links = {}

        res.locals.hateoas({
            ...response,
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
