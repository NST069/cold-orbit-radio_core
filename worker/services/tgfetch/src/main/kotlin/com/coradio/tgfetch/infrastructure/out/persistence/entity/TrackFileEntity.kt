package com.coradio.tgfetch.infrastructure.out.persistence.entity

import com.coradio.tgfetch.domain.enums.TrackFileStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "track_files")
class TrackFileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    var id: UUID? = null

    @Column(name = "etag", nullable = false)
    lateinit var etag: String

    @Column(name = "telegram_file_id", nullable = false)
    lateinit var telegramFileId: String

    @Column(name = "telegram_file_unique_id", nullable = false)
    lateinit var telegramFileUniqueId: String

    @Column(name = "storage_key")
    var storageKey: String? = null

    @Column(name = "file_name", nullable = false)
    lateinit var fileName: String

    @Column(name = "file_size", nullable = false)
    var fileSize: Long = 0

    @Column(name = "mime_type", nullable = false)
    lateinit var mimeType: String

    @Column(name = "downloaded_at", nullable = false)
    var downloadedAt: Instant = Instant.now()

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    var status: TrackFileStatus = TrackFileStatus.CREATED

    @Column(name = "retry_count")
    var retryCount: Int = 0

    @Column(name = "last_download_attempt_at")
    var lastDownloadAttemptAt: Instant? = null

    @OneToOne
    @JoinColumn(name = "track_id", nullable = false)
    lateinit var trackEntity: TrackEntity

    @OneToMany(mappedBy = "trackFileEntity")
    lateinit var analysisJobEntities: MutableList<AnalysisJobEntity>

}
