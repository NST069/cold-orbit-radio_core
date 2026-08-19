const client = require('./client')

const logger = require('../services/logger')

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

async function removeFileByRemoteId(remoteFileId) {
    logger.info("Removing file", remoteFileId)

    const file = await client.invoke({
        '@type': 'getRemoteFile',
        remote_file_id: remoteFileId
    })

    if (!file.id) {
        return
    }

    await client.invoke({
        '@type': 'removeFileFromDownloads',
        file_id: file.id,
        delete_from_cache: true
    })
}

async function removeFile(fileId) {
    logger.info({ fileId }, 'Removing file')

    const file = await client.invoke({
        '@type': 'getFile',
        file_id: Number(fileId)
    })

    logger.info({
        fileId: file.id,
        path: file.local?.path,
        exists: !!file.local,
        downloaded: file.local?.is_downloading_completed
    }, 'File before removal')

    await client.invoke({
        '@type': 'removeFileFromDownloads',
        file_id: Number(fileId),
        delete_from_cache: true
    })
}

module.exports = {
    downloadFile,
    removeFileByRemoteId,
    removeFile
}
