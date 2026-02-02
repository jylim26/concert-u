package com.concertu.ticketing.reservation.domain;

import jakarta.persistence.*;
import lombok.*;
import com.concertu.ticketing.concert.domain.Performance;
import com.concertu.ticketing.concert.domain.Seat;
import com.concertu.ticketing.global.domain.BaseEntity;

@Getter
@Entity
@Table(name = "reservations", uniqueConstraints = {
		@UniqueConstraint(name = "uk_reservation_perf_seat", columnNames = {"performance_id", "seat_id"})
})
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "performance_id", nullable = false)
	private Performance performance;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "seat_id", nullable = false)
	private Seat seat;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ReservationStatus status;

	public static Reservation create(Performance performance, Seat seat, Long userId, ReservationStatus status) {
		return new Reservation(null, performance, seat, userId, status);
	}
}
