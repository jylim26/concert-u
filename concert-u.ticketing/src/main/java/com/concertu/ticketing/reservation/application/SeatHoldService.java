package com.concertu.ticketing.reservation.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SeatHoldService {

	private final SeatHoldTxService seatHoldTxService;

	public void hold(Long performanceId, Long seatId, Long userId) {
		holdWithPessimistic(performanceId, seatId, userId);
	}

	public void holdWithPessimistic(Long performanceId, Long seatId, Long userId) {
		seatHoldTxService.holdPessimistic(performanceId, seatId, userId);
	}

	public void holdWithOptimistic(Long performanceId, Long seatId, Long userId) {
		seatHoldTxService.holdOptimistic(performanceId, seatId, userId);
	}
}
