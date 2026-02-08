package com.concertu.ticketing.reservation.api;

import com.concertu.ticketing.reservation.application.PerformanceSeatQueryService;
import com.concertu.ticketing.reservation.application.SeatHoldService;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/performances/{performanceId}/seats")
public class SeatHoldController {

	private final SeatHoldService seatHoldService;
	private final PerformanceSeatQueryService performanceSeatQueryService;

	@GetMapping
	public List<PerformanceSeatView> list(@PathVariable Long performanceId) {
		return performanceSeatQueryService.listSeats(performanceId);
	}

	@PostMapping("/{seatId}/hold")
	public void hold(@PathVariable Long performanceId, @PathVariable Long seatId, @RequestBody HoldSeatRequest request) {
		seatHoldService.hold(performanceId, seatId, request.userId());
	}

	@PostMapping("/{seatId}/hold/pessimistic")
	public void holdWithPessimistic(
			@PathVariable Long performanceId, @PathVariable Long seatId, @RequestBody HoldSeatRequest request) {
		seatHoldService.holdWithPessimistic(performanceId, seatId, request.userId());
	}

	@PostMapping("/{seatId}/hold/optimistic")
	public void holdWithOptimistic(
			@PathVariable Long performanceId, @PathVariable Long seatId, @RequestBody HoldSeatRequest request) {
		seatHoldService.holdWithOptimistic(performanceId, seatId, request.userId());
	}

	public record HoldSeatRequest(Long userId) {}
}
