package com.concertu.ticketing.reservation.domain.exception;

public class PerformanceSeatNotFoundException extends RuntimeException {

	public PerformanceSeatNotFoundException(Long performanceId, Long seatId) {
		super("PerformanceSeat not found. performanceId=%d, seatId=%d".formatted(performanceId, seatId));
	}
}

