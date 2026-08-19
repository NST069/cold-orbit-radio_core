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

            reply.header(
                'X-Telegram-File-Id',
                file.fileId
            )

            reply.header(
                'Content-Length',
                file.size
            )

            const stream =
                fs.createReadStream(file.path)

            return reply.send(stream)
        }
    )

    fastify.delete(
        '/files/by-remote/:remoteFileId',
        {
            schema: {
                tags: ['Files'],
                summary: 'Delete file from local cache by remote file id',
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
                    204: {
                        description: "File deleted or already gone"
                    }
                }
            }
        },
        async (request, reply) => {

            await downloads.removeFileByRemoteId(
                request.params.remoteFileId
            )

            return reply.code(204).send()
        }
    )

    fastify.delete(
        '/files/:fileId',
        {
            schema: {
                tags: ['Files'],
                summary: 'Delete file from local cache by file id',
                params: {
                    type: 'object',
                    required: ['fileId'],
                    properties: {
                        fileId: {
                            type: 'string'
                        }
                    }
                },
                response: {
                    204: {
                        description: "File deleted or already gone"
                    }
                }
            }
        },
        async (request, reply) => {

            await downloads.removeFile(
                request.params.fileId
            )

            return reply.code(204).send()
        }
    )
}

module.exports = routes
