package com.coradio.rotation.infrastructure.out.persistense.entity;

import com.coradio.rotation.domain.enums.PlaybackStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Table;
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
@Table(name = "track_queue")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrackQueueEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "track_id", nullable = false)
    private UUID trackId;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private PlaybackStatus status;

    @Column(name = "last_error")
    private String lastError;

    @Column(name = "localPath")
    private String localPath;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

}
