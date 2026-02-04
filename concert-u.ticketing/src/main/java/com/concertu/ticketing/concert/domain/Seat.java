package com.concertu.ticketing.concert.domain;

import com.concertu.ticketing.global.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "seats", uniqueConstraints = {
		@UniqueConstraint(name = "uk_seat_venue_no", columnNames = {"venue_id", "seat_no"})
})
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Seat extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "seat_no", nullable = false)
	private Long seatNo;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "venue_id", nullable = false)
	private Venue venue;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "grade_id", nullable = false)
	private SeatGrade grade;

	public static Seat create(Long seatNo, Venue venue, SeatGrade grade) {
		return new Seat(null, seatNo, venue, grade);
	}
}
