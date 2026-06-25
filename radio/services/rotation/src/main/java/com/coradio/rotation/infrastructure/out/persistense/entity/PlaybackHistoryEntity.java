package com.coradio.rotation.infrastructure.out.persistense.entity;

import jakarta.persistence.Entity;
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
@Table(name = "playback_history")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlaybackHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "track_id", nullable = false)
    private UUID trackId;

    @Column(name = "artist", nullable = false)
    private String artist;

    @Column(name = "title", nullable = false)
    private String title;

    @CreatedDate
    @Column(name = "played_at", nullable = false)
    private Instant playedAt;

}
