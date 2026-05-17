package com.coradio.tgfetch.infrastructure.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "track_files")
class TrackFileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    var id: UUID? = null

    @OneToOne
    @JoinColumn(name = "track_id", nullable = false)
    lateinit var trackEntity: TrackEntity

    @Column(name = "sha256", nullable = false)
    lateinit var sha256: String

    @Column(name = "telegram_file_unique_id", nullable = false)
    lateinit var telegramFileUniqueId: String

    @Column(name = "storage_key")
    var storageKey: String? = null

    @Column(name = "file_size", nullable = false)
    var fileSize: Long = 0

    @Column(name = "mime_type", nullable = false)
    lateinit var mimeType: String

    @CreatedDate
    @Column(name = "uploaded_at", nullable = false)
    lateinit var uploadedAt: OffsetDateTime

    @OneToMany(mappedBy = "trackFileEntity")
    lateinit var analysisJobEntities: MutableList<AnalysisJobEntity>

    @OneToOne(mappedBy = "trackFileEntity")
    lateinit var telegramPostEntity: TelegramPostEntity
}
