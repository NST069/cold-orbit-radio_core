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
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "telegram_posts")
class TelegramPostEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    var id: UUID? = null

    @Column(name = "channel_id", nullable = false)
    var channelId: Long = 0

    @Column(name = "message_id", nullable = false)
    var messageId: Long = 0

    @OneToOne
    @JoinColumn(name = "track_id", nullable = false)
    lateinit var trackEntity: TrackEntity

    @OneToOne
    @JoinColumn(name = "track_file_id", nullable = false)
    lateinit var trackFileEntity: TrackFileEntity

    @Column(name = "raw_text")
    var rawText: String? = null

    @Column(name = "published_at", nullable = false)
    lateinit var publishedAt: Instant

    @CreatedDate
    @Column(name = "fetched_at", nullable = false)
    lateinit var fetchedAt: Instant
}
