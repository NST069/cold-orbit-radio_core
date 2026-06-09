package com.coradio.tgfetch.infrastructure.out.persistence.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToOne
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
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

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()

    @OneToOne(
        mappedBy = "trackEntity",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var trackFileEntity: TrackFileEntity? = null

    @OneToOne(
        mappedBy = "trackEntity",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    var telegramPostEntity: TelegramPostEntity? = null

    @PreUpdate
    fun preUpdate() {
        updatedAt = Instant.now()
    }

    fun attachTrackFile(file: TrackFileEntity) {
        trackFileEntity = file
        file.trackEntity = this
    }

    fun attachTelegramPost(post: TelegramPostEntity) {
        telegramPostEntity = post
        post.trackEntity = this
    }
}
