package com.coradio.rotation.infrastructure.out.persistense.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;
import java.util.UUID;

@Entity
@Table(name = "track_stats")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrackStatsEntity {
    @Id
    @Column(name = "track_id", nullable = false)
    private UUID trackId;

    @Column(name = "plays", nullable = false)
    private BigInteger plays;

    @Column(name = "likes", nullable = false)
    private BigInteger likes;

}
