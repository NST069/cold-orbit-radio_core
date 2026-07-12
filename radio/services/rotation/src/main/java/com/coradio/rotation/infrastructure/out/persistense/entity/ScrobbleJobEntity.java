package com.coradio.rotation.infrastructure.out.persistense.entity;

import com.coradio.rotation.domain.enums.JobStatus;
import com.coradio.rotation.domain.enums.ScrobblerProvider;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Table;
import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "scrobble_jobs")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScrobbleJobEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "playback_id")
    private PlaybackHistoryEntity playbackHistoryEntity;

    @Column(name = "provider", nullable = false)
    @Enumerated(EnumType.STRING)
    private ScrobblerProvider provider;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private JobStatus status;

    @CreatedDate
    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "attempts", nullable = false)
    private int attempts = 0;

    @Column(name = "error")
    private String error;

}
