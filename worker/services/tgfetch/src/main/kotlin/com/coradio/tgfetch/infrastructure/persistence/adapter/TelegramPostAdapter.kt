package com.coradio.tgfetch.infrastructure.persistence.adapter

import com.coradio.tgfetch.domain.model.TelegramPost
import com.coradio.tgfetch.domain.port.out.persistence.TelegramPostRepositoryPort
import com.coradio.tgfetch.infrastructure.persistence.mapper.TelegramPostMapper
import com.coradio.tgfetch.infrastructure.persistence.repository.TelegramPostRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class TelegramPostAdapter(
    private val telegramPostRepository: TelegramPostRepository
): TelegramPostRepositoryPort {
    override fun save(telegramPost: TelegramPost): TelegramPost {
        val entity = TelegramPostMapper.toEntity(telegramPost)
        val saved = telegramPostRepository.save(entity)
        return TelegramPostMapper.toDomain(saved)
    }

    override fun findById(id: UUID): TelegramPost? {
        return telegramPostRepository.findById(id)
            .map(TelegramPostMapper::toDomain)
            .orElse(null)
    }

    override fun findAll(): List<TelegramPost> {
        return telegramPostRepository.findAll()
            .map(TelegramPostMapper::toDomain)
    }

    override fun deleteById(id: UUID) {
        telegramPostRepository.deleteById(id)
    }
}
