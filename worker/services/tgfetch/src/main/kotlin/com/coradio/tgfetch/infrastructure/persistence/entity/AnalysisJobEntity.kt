package com.coradio.tgfetch.infrastructure.persistence.entity

import com.coradio.tgfetch.domain.enums.AnalysisJobStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import java.time.Instant
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
    @Enumerated(EnumType.STRING)
    var status: AnalysisJobStatus = AnalysisJobStatus.CREATED

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    lateinit var createdAt: Instant

    @Column(name = "started_at")
    var startedAt: Instant? = null

    @Column(name = "finished_at")
    var finishedAt: Instant? = null

    @Column(name = "error_message")
    var errorMessage: String? = null
}
