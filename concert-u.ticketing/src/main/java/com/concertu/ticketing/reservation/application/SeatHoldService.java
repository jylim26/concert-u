package com.concertu.ticketing.reservation.application;

import java.util.concurrent.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SeatHoldService {

	private final SeatHoldTxService seatHoldTxService;
	private final ConcurrentMap<Long, Object> seatLocks = new ConcurrentHashMap<>();

	public void hold(Long performanceId, Long seatId, Long userId) {
		Object lock = seatLocks.computeIfAbsent(seatId, key -> new Object());

		synchronized (lock) {
			// 락 대기 시간 방지를 위해 별도 트랜잭션으로 분리
			seatHoldTxService.hold(performanceId, seatId, userId);
		}
	}
}
