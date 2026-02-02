package com.concertu.ticketing.concert.domain;

import com.concertu.ticketing.global.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Entity
@Table(name = "performances", indexes = {
        @Index(name = "idx_performance_concert_start", columnList = "concert_id, start_at")
})
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Performance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "concert_id", nullable = false)
    private Concert concert;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    public static Performance create(Concert concert, Venue venue, Instant startAt) {
        return new Performance(null, concert, venue, startAt);
    }
}
