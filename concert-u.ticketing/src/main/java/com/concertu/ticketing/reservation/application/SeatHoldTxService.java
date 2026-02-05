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
	public void hold(Long performanceId, Long seatId, Long userId) {
		PerformanceSeat performanceSeat = performanceSeatRepository
				.findByPerformanceIdAndSeatId(performanceId, seatId)
				.orElseThrow(() -> new PerformanceSeatNotFoundException(performanceId, seatId));

		if (!performanceSeat.isAvailable()) throw new SeatNotAvailableException(performanceId, seatId);
		performanceSeat.hold(userId, Instant.now(clock));
	}
}

