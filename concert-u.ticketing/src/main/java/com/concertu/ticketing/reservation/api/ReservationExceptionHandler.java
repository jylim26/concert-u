package com.concertu.ticketing.reservation.api;

import com.concertu.ticketing.reservation.domain.exception.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class ReservationExceptionHandler {

	@ExceptionHandler(PerformanceSeatNotFoundException.class)
	public ResponseEntity<String> handleNotFound(PerformanceSeatNotFoundException e) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
	}

	@ExceptionHandler(SeatNotAvailableException.class)
	public ResponseEntity<String> handleConflict(SeatNotAvailableException e) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
	}
}
