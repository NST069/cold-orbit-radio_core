const client = require('./client')

const logger = require('../services/logger')

const fs = require('fs/promises')

async function downloadFile(
    remoteFileId
) {
    for (let attempt = 1; attempt <= 5; attempt++) {

        try {

            const file = await client.invoke({
                '@type': 'getRemoteFile',
                remote_file_id: remoteFileId
            })

            const downloaded = await download(file.id)

            logger.info({
                fileId: downloaded.fileId,
                remoteFileId: remoteFileId,
                path: downloaded.path,
            }, 'File downloaded')

            return downloaded

        } catch (err) {

            if (
                err.code === 400 &&
                attempt < 5
            ) {
                await new Promise(
                    resolve =>
                        setTimeout(
                            resolve,
                            150 * attempt
                        )
                )

                continue
            }

            logger.error({
                err,
                remoteFileId
            }, 'File download failed')

            throw err
        }
    }
}

async function download(
    fileId
) {
    logger.info("download started")
    const downloaded = await client.invoke({
        '_': 'downloadFile',
        file_id: fileId,
        priority: 1
    })

    const file = await waitForFile(fileId)

    return {
        fileId: fileId,
        path: file.local.path,
        size: file.size
    }

}

async function waitForFile(fileId) {
    for (let i = 0; i < 50; i++) {
        const file = await client.invoke({
            '@type': 'getFile',
            file_id: fileId
        })

        if (file.local?.is_downloading_completed) {
            return file
        }

        await new Promise(r => setTimeout(r, 200))
    }

    logger.error("File download timeout")

    throw new Error("File download timeout")
}

async function removeFile(fileId) {
    logger.info({ fileId }, 'Removing file')

    const file = await client.invoke({
        '@type': 'getFile',
        file_id: fileId
    })

    const path = file.local?.path

    if (!path) {
        logger.info({ fileId }, 'File already gone')
        return
    }

    await fs.rm(path, { force: true })

    logger.info({
        fileId,
        path
    }, 'File removed')
}

module.exports = {
    downloadFile,
    removeFile
}
