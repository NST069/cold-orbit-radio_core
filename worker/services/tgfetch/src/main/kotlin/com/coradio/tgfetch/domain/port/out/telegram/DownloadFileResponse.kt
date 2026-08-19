package com.coradio.tgfetch.domain.port.out.telegram

import java.nio.file.Path

data class DownloadFileResponse(
    val path: Path,
    val fileId: String
)