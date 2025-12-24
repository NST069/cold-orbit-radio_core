const swaggerJSDoc = require('swagger-jsdoc')
const swaggerUi = require('swagger-ui-express')

const options = {
  definition: {
    openapi: '3.0.0',
    info: {
      title: 'Cold Orbit Radio API',
      version: '1.0.0',
      description: 'Radio Station API',
    },
    servers: [
      { url: 'http://localhost:3000/api/v1', description: 'API V1' },
    ],
  },
  apis: ['./routes/v1/*.js'],
}

const swaggerSpec = swaggerJSDoc(options)

const swaggerOptions = {
  customSiteTitle: "Cold Orbit Radio API",
}

module.exports = {
  spec: swaggerSpec,
  setup: swaggerUi.setup(swaggerSpec, swaggerOptions),
  serve: swaggerUi.serve
}
