package com.concertu.ticketing.reservation.domain;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;
import com.concertu.ticketing.concert.domain.Performance;
import com.concertu.ticketing.concert.domain.Seat;
import com.concertu.ticketing.global.domain.BaseEntity;

@Getter
@Entity
@Table(name = "performance_seats", uniqueConstraints = {
		@UniqueConstraint(name = "uk_perf_seat", columnNames = {"performance_id", "seat_id"})
}, indexes = {
		@Index(name = "idx_perf_status", columnList = "performance_id,status")
})
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PerformanceSeat extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "performance_id", nullable = false)
	private Performance performance;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "seat_id", nullable = false)
	private Seat seat;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PerformanceSeatStatus status;

	@Column(name = "reserved_by")
	private Long reservedBy;

	@Column(name = "reserved_at")
	private Instant reservedAt;

	@Version
	@Column(nullable = false)
	private Long version;

	public static PerformanceSeat create(Performance performance, Seat seat) {
		return new PerformanceSeat(null, performance, seat, PerformanceSeatStatus.AVAILABLE, null, null, null);
	}

	public boolean isAvailable() {
		return status == PerformanceSeatStatus.AVAILABLE;
	}

	public void hold(Long userId, Instant now) {
		this.status = PerformanceSeatStatus.HELD;
		this.reservedBy = userId;
		this.reservedAt = now;
	}
}
