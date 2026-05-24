package com.coradio.tgfetch.infrastructure.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "tracks")
class TrackEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    var id: UUID? = null

    @Column(name = "title", nullable = false)
    lateinit var title: String

    @Column(name = "artist", nullable = false)
    lateinit var artist: String

    @Column(name = "duration", nullable = false)
    var duration: Int = 0

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    lateinit var createdAt: Instant

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: Instant

    @OneToOne(mappedBy = "trackEntity")
    lateinit var trackFileEntity: TrackFileEntity

    @OneToOne(mappedBy = "trackEntity")
    lateinit var telegramPostEntity: TelegramPostEntity
}
