package com.concertu.ticketing.reservation.api;

import com.concertu.ticketing.reservation.domain.PerformanceSeatStatus;
import java.time.*;

public record PerformanceSeatView(
		Long seatId,
		Long seatNo,
		String gradeName,
		Long price,
		PerformanceSeatStatus status,
		Long reservedBy,
		Instant reservedAt
) {}
