package com.concertu.ticketing.reservation.domain.exception;

public class SeatNotAvailableException extends RuntimeException {

	public SeatNotAvailableException(Long performanceId, Long seatId) {
		super("Seat not available. performanceId=%d, seatId=%d".formatted(performanceId, seatId));
	}
}

