package com.coradio.tgfetch.infrastructure.out.persistence.adapter

import com.coradio.tgfetch.domain.model.TelegramPost
import com.coradio.tgfetch.domain.model.view.TelegramPostView
import com.coradio.tgfetch.domain.port.out.persistence.TelegramPostRepositoryPort
import com.coradio.tgfetch.infrastructure.out.persistence.mapper.TelegramPostMapper
import com.coradio.tgfetch.infrastructure.out.persistence.repository.TelegramPostRepository
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

    override fun findByChannelAndMessageId(
        channelId: Long,
        messageId: Long
    ): TelegramPostView? {
        val entity = telegramPostRepository.findByChannelIdAndMessageId(channelId, messageId)
        return if (entity != null)
            TelegramPostMapper.toTelegramPostView(entity)
        else null
    }

    override fun existsByChannelAndMessageId(
        channelId: Long,
        messageId: Long
    ): Boolean = telegramPostRepository.existsByChannelIdAndMessageId(channelId, messageId)

    override fun saveAll(newMessages: List<TelegramPost>) {
        telegramPostRepository.saveAll(newMessages.map { TelegramPostMapper.toEntity(it) })
    }
}
