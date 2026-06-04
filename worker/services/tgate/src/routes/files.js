const downloads = require('../telegram/downloads')
const fs = require('fs')

async function routes(fastify, opts) {

    const cleaner = opts.cleaner

    fastify.get(
        '/files/by-remote/:remoteFileId',
        {
            schema: {
                tags: ['Files'],
                summary: 'Download file by TDLib remote file id',
                params: {
                    type: 'object',
                    required: ['remoteFileId'],
                    properties: {
                        fileId: {
                            type: 'string'
                        }
                    }
                },
                response: {
                    200: {
                        description: 'Binary file'
                    }
                }
            }
        },
        async (request, reply) => {

            const file =
                await downloads.downloadFile(
                    request.params.remoteFileId
                )

            const stream =
                fs.createReadStream(file.path)

            return reply.send(stream)
        }
    )
}

module.exports = routes
