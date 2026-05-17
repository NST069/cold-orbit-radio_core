package com.coradio.tgfetch.application.service

import com.coradio.tgfetch.domain.model.TelegramPost
import com.coradio.tgfetch.domain.port.out.TelegramPostRepositoryPort
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class TelegramPostService(
    private val telegramPostRepository: TelegramPostRepositoryPort,
) {
    fun saveTelegramPost(telegramPost: TelegramPost): TelegramPost = telegramPostRepository.save(telegramPost)

    fun findPostById(id: UUID): TelegramPost? = telegramPostRepository.findById(id)

    fun findAllPosts(): List<TelegramPost> = telegramPostRepository.findAll()

    fun deleteById(id: UUID) = telegramPostRepository.deleteById(id)

}
