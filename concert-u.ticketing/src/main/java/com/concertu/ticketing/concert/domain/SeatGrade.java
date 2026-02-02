package com.concertu.ticketing.concert.domain;

import com.concertu.ticketing.global.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Table(name = "seat_grades", uniqueConstraints = {
		@UniqueConstraint(name = "uk_seat_grade_name", columnNames = "name")
})
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SeatGrade extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private Long price;

	public static SeatGrade create(String name, Long price) {
		return new SeatGrade(null, name, price);
	}
}
