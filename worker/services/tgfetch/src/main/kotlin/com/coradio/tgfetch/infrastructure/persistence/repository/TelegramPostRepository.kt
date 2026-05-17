package com.coradio.tgfetch.infrastructure.persistence.repository

import com.coradio.tgfetch.infrastructure.persistence.entity.TelegramPostEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface TelegramPostRepository : JpaRepository<TelegramPostEntity, UUID> {
}
