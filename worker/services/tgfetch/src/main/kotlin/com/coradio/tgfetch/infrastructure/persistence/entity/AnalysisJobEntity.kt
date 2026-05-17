package com.coradio.tgfetch.infrastructure.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "analysis_jobs")
class AnalysisJobEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    var id: UUID? = null

    @ManyToOne
    @JoinColumn(name = "track_file_id", nullable = false)
    lateinit var trackFileEntity: TrackFileEntity

    @Column(name = "status", nullable = false)
    lateinit var status: String

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    lateinit var createdAt: OffsetDateTime

    @Column(name = "started_at")
    var startedAt: OffsetDateTime? = null

    @Column(name = "finished_at")
    var finishedAt: OffsetDateTime? = null

    @Column(name = "error_message")
    var errorMessage: String? = null
}
