
exports.hateoasMiddleware = (req, res, next) => {

    res.hateoas = (data, links = {}) => {
        const baseUrl = `${req.protocol}://${req.get("host")}`

        const selfLink = {
            href: `${baseUrl + req.originalUrl}`,
            method: req.method
        }

        const docs = {
            href: `${req.protocol}://${req.get("host")}/api-docs`,
            method: "GET"
        }

        const response = {
            ...data,
            _links: {
                self: selfLink,
                docs: docs,
                ...links
            },
            _meta: {
                timestamp: new Date().toISOString(),
                version: "1.0"
            }
        }

        res.json(response)
    }
    next()
}
