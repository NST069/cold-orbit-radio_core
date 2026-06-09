function mapMessage(message) {
    return {
        channelId: message.chat_id,
        messageId: message.id,
        date: message.date,

        type: message.content._,

        text: message.content.caption?.text || message.content.text?.text || null,

        audio: message.content.audio ? {
            title: message.content.audio.title,
            performer: message.content.audio.performer,
            durationSeconds: message.content.audio.duration,
            fileSizeBytes: message.content.audio.audio?.size,

            fileName: message.content.audio.file_name,
            mimeType: message.content.audio.mime_type,

            tdFileId: message.content.audio.audio?.id,
            remoteFileId: message.content.audio.audio?.remote?.id,
            uniqueFileId: message.content.audio.audio?.remote?.unique_id
        } : null,

        cover: message.content.audio?.album_cover_thumbnail ? {
            tdFileId: message.content.audio.album_cover_thumbnail.file?.id,
            remoteFileId: message.content.audio.album_cover_thumbnail.file?.remote?.id,
            uniqueFileId: message.content.audio.album_cover_thumbnail.file?.remote?.unique_id
        } : null,

        caption: message.content.caption?.text || null
    }
}

module.exports = {
    mapMessage
}
