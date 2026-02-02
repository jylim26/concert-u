package com.concertu.ticketing.reservation.domain;

import com.concertu.ticketing.global.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Table(name = "payments", uniqueConstraints = {
		@UniqueConstraint(name = "uk_payment_order_id", columnNames = "order_id")
})
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "order_id", nullable = false)
	private String orderId;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(nullable = false)
	private Long amount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentStatus status;

	public static Payment create(String orderId, Long userId, Long amount, PaymentStatus status) {
		return new Payment(null, orderId, userId, amount, status);
	}
}
