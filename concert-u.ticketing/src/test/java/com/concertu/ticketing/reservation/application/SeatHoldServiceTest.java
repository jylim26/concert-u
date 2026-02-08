package com.concertu.ticketing.reservation.application;

import com.concertu.ticketing.concert.domain.Concert;
import com.concertu.ticketing.concert.domain.Performance;
import com.concertu.ticketing.concert.domain.Seat;
import com.concertu.ticketing.concert.domain.SeatGrade;
import com.concertu.ticketing.concert.domain.Venue;
import com.concertu.ticketing.concert.infra.*;
import com.concertu.ticketing.reservation.domain.PerformanceSeat;
import com.concertu.ticketing.reservation.infra.PerformanceSeatJpaRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class SeatHoldServiceTest {

	@Autowired
	private SeatHoldService seatHoldService;

	@Autowired
	private PerformanceSeatJpaRepository performanceSeatRepository;

	@Autowired
	private PerformanceJpaRepository performanceRepository;

	@Autowired
	private SeatJpaRepository seatRepository;

	@Autowired
	private ConcertJpaRepository concertRepository;

	@Autowired
	private SeatGradeJpaRepository seatGradeRepository;

	@Autowired
	private VenueJpaRepository venueRepository;

	private Long performanceId;
	private Long seatId;

	@BeforeEach
	void setUp() {
		performanceSeatRepository.deleteAllInBatch();
		performanceRepository.deleteAllInBatch();
		seatRepository.deleteAllInBatch();
		concertRepository.deleteAllInBatch();
		seatGradeRepository.deleteAllInBatch();
		venueRepository.deleteAllInBatch();

		Venue venue = venueRepository.save(Venue.create("잠실 종합운동장"));
		SeatGrade grade = seatGradeRepository.save(SeatGrade.create("R", 100_000L));
		Concert concert = concertRepository.save(Concert.create("아이유 콘서트"));
		Performance performance = performanceRepository.save(
				Performance.create(concert, venue, Instant.now().plus(1, ChronoUnit.DAYS)));
		Seat seat = seatRepository.save(Seat.create(1L, venue, grade));
		performanceSeatRepository.save(PerformanceSeat.create(performance, seat));

		performanceId = performance.getId();
		seatId = seat.getId();
	}

	@Test
	@DisplayName("동일 좌석에 비관적 락으로 동시 선점 요청하면 1건만 성공한다")
	void givenSameSeat_whenPessimisticHold_thenOneSuccess() throws Exception {
		// given
		int users = 100;
		ExecutorService executor = Executors.newFixedThreadPool(users);
		CountDownLatch ready = new CountDownLatch(users);
		CountDownLatch start = new CountDownLatch(1);
		CountDownLatch done = new CountDownLatch(users);
		AtomicInteger success = new AtomicInteger();
		AtomicInteger failure = new AtomicInteger();
		long startedAt = System.nanoTime();

		// when
		for (int i = 0; i < users; i++) {
			long userId = i + 1L;
			executor.submit(() -> {
				ready.countDown();
				start.await();
				try {
					seatHoldService.holdWithPessimistic(performanceId, seatId, userId);
					success.incrementAndGet();
				} catch (RuntimeException e) {
					failure.incrementAndGet();
				} finally {
					done.countDown();
				}
				return null;
			});
		}

		// then
		assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
		start.countDown();
		assertThat(done.await(10, TimeUnit.SECONDS)).isTrue();
		executor.shutdown();
		long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
		System.out.println("[비관적락] 총소요시간(ms)=" + elapsedMs);

		assertThat(success.get()).isEqualTo(1);
		assertThat(failure.get()).isEqualTo(users - 1);
	}

	@Test
	@DisplayName("동일 좌석에 낙관적 락으로 동시 선점 요청하면 1건만 성공한다")
	void givenSameSeat_whenOptimisticHold_thenOneSuccess() throws Exception {
		// given
		int users = 100;
		ExecutorService executor = Executors.newFixedThreadPool(users);
		CountDownLatch ready = new CountDownLatch(users);
		CountDownLatch start = new CountDownLatch(1);
		CountDownLatch done = new CountDownLatch(users);
		AtomicInteger success = new AtomicInteger();
		AtomicInteger failure = new AtomicInteger();
		long startedAt = System.nanoTime();

		// when
		for (int i = 0; i < users; i++) {
			long userId = i + 1L;
			executor.submit(() -> {
				ready.countDown();
				start.await();
				try {
					seatHoldService.holdWithOptimistic(performanceId, seatId, userId);
					success.incrementAndGet();
				} catch (RuntimeException e) {
					failure.incrementAndGet();
				} finally {
					done.countDown();
				}
				return null;
			});
		}

		// then
		assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
		start.countDown();
		assertThat(done.await(10, TimeUnit.SECONDS)).isTrue();
		executor.shutdown();
		long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
		System.out.println("[낙관적락] 총소요시간(ms)=" + elapsedMs);
		assertThat(success.get()).isEqualTo(1);
		assertThat(failure.get()).isEqualTo(users - 1);
	}
}
