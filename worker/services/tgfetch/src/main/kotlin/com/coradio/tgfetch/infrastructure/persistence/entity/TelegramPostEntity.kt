package com.coradio.tgfetch.infrastructure.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "telegram_posts")
class TelegramPostEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    var id: UUID? = null

    @Column(name = "telegram_post_id", nullable = false)
    lateinit var telegramPostId: String

    @OneToOne
    @JoinColumn(name = "track_id", nullable = false)
    lateinit var trackEntity: TrackEntity

    @OneToOne
    @JoinColumn(name = "track_file_id", nullable = false)
    lateinit var trackFileEntity: TrackFileEntity

    @Column(name = "raw_text")
    var rawText: String? = null

    @CreatedDate
    @Column(name = "fetched_at", nullable = false)
    lateinit var fetchedAt: OffsetDateTime
}
