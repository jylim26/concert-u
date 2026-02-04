package com.concertu.ticketing.bootstrap;

import com.concertu.ticketing.concert.domain.*;
import com.concertu.ticketing.concert.infra.*;
import com.concertu.ticketing.reservation.domain.PerformanceSeat;
import com.concertu.ticketing.reservation.infra.PerformanceSeatJpaRepository;
import java.time.*;
import java.time.temporal.ChronoUnit;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@Profile("local")
public class LocalSeedDataRunner {

	@Bean
	ApplicationRunner seedData(SeedService seedService) {
		return args -> seedService.seedIfEmpty();
	}

	@Component
	@Profile("local")
	@RequiredArgsConstructor
	static class SeedService {

		private final ConcertJpaRepository concertRepository;
		private final PerformanceJpaRepository performanceRepository;
		private final VenueJpaRepository venueRepository;
		private final SeatGradeJpaRepository seatGradeRepository;
		private final SeatJpaRepository seatRepository;
		private final PerformanceSeatJpaRepository performanceSeatRepository;

		@Transactional
		public void seedIfEmpty() {
			if (performanceSeatRepository.count() > 0) {
				return;
			}

			Venue venue = venueRepository.save(Venue.create("Sample Venue"));
			SeatGrade grade = seatGradeRepository.save(SeatGrade.create("R", 100_000L));
			Concert concert = concertRepository.save(Concert.create("Sample Concert"));
			Performance performance = performanceRepository.save(
					Performance.create(concert, venue, Instant.now().plus(1, ChronoUnit.DAYS))
			);

			Seat seat = seatRepository.save(Seat.create(1L, venue, grade));
			performanceSeatRepository.save(PerformanceSeat.create(performance, seat));
		}
	}
}
