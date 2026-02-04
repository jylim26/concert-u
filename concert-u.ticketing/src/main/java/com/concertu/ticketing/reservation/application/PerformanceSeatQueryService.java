package com.concertu.ticketing.reservation.application;

import com.concertu.ticketing.reservation.api.PerformanceSeatView;
import com.concertu.ticketing.reservation.infra.PerformanceSeatJpaRepository;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PerformanceSeatQueryService {

	private final PerformanceSeatJpaRepository performanceSeatRepository;

	public List<PerformanceSeatView> listSeats(Long performanceId) {
		return performanceSeatRepository.findSeatViewsByPerformanceId(performanceId);
	}
}
