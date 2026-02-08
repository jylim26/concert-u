package com.concertu.ticketing.reservation.application;

import com.concertu.ticketing.reservation.domain.PerformanceSeat;
import com.concertu.ticketing.reservation.domain.exception.*;
import com.concertu.ticketing.reservation.infra.PerformanceSeatJpaRepository;
import java.time.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SeatHoldTxService {

	private final PerformanceSeatJpaRepository performanceSeatRepository;
	private final Clock clock;

	@Transactional
	public void holdPessimistic(Long performanceId, Long seatId, Long userId) {
		PerformanceSeat performanceSeat = performanceSeatRepository
				.findForUpdateByPerformanceIdAndSeatId(performanceId, seatId)
				.orElseThrow(() -> new PerformanceSeatNotFoundException(performanceId, seatId));
		holdSeat(performanceId, seatId, userId, performanceSeat);
	}

	@Transactional
	public void holdOptimistic(Long performanceId, Long seatId, Long userId) {
		PerformanceSeat performanceSeat = performanceSeatRepository
				.findByPerformanceIdAndSeatId(performanceId, seatId)
				.orElseThrow(() -> new PerformanceSeatNotFoundException(performanceId, seatId));
		holdSeat(performanceId, seatId, userId, performanceSeat);
	}

	private void holdSeat(Long performanceId, Long seatId, Long userId, PerformanceSeat performanceSeat) {
		if (!performanceSeat.isAvailable()) throw new SeatNotAvailableException(performanceId, seatId);
		performanceSeat.hold(userId, Instant.now(clock));
	}
}
