package com.coradio.tgfetch.domain.port.out

import com.coradio.tgfetch.domain.model.TelegramPost
import java.util.UUID

interface TelegramPostRepositoryPort {
    fun save(telegramPost: TelegramPost): TelegramPost
    fun findById(id: UUID): TelegramPost?
    fun findAll(): List<TelegramPost>
    fun deleteById(id: UUID)
}
