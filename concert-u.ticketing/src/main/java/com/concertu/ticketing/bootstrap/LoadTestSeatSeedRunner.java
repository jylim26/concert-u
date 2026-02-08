package com.concertu.ticketing.bootstrap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.loadtest.enabled", havingValue = "true")
public class LoadTestSeatSeedRunner implements ApplicationRunner {

	private final JdbcTemplate jdbcTemplate;

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		int seatCount = 30000;
		long venueId = 1L;
		long gradeId = 1L;
		long concertId = 1L;
		long performanceId = 1L;

		jdbcTemplate.update(
				"insert into venues (id, name, created_at, updated_at) values (?, ?, now(), now()) " +
						"on duplicate key update name = values(name), updated_at = values(updated_at)",
				venueId,
				"LoadTest Venue");
		jdbcTemplate.update(
				"insert into seat_grades (id, name, price, created_at, updated_at) values (?, ?, ?, now(), now()) " +
						"on duplicate key update name = values(name), price = values(price), updated_at = values(updated_at)",
				gradeId,
				"R",
				100000L);
		jdbcTemplate.update(
				"insert into concerts (id, title, created_at, updated_at) values (?, ?, now(), now()) " +
						"on duplicate key update title = values(title), updated_at = values(updated_at)",
				concertId,
				"LoadTest Concert");
		jdbcTemplate.update(
				"insert into performances (id, concert_id, venue_id, start_at, created_at, updated_at) values (?, ?, ?, date_add(now(), interval 1 day), now(), now()) " +
						"on duplicate key update concert_id = values(concert_id), venue_id = values(venue_id), start_at = values(start_at), updated_at = values(updated_at)",
				performanceId,
				concertId,
				venueId);

		Integer exists = jdbcTemplate.queryForObject(
				"select count(*) from performance_seats where performance_id = ?",
				Integer.class,
				performanceId);
		if (exists != null && exists >= seatCount) {
			log.info("loadtest seed skipped. performanceId={}, existingSeats={}", performanceId, exists);
			return;
		}

		jdbcTemplate.update("set session cte_max_recursion_depth = ?", seatCount + 1000);
		jdbcTemplate.update(
				"""
				insert into seats (seat_no, venue_id, grade_id, created_at, updated_at)
				with recursive seq as (
				  select 1 as n
				  union all
				  select n + 1 from seq where n < ?
				)
				select n, ?, ?, now(), now()
				from seq
				on duplicate key update id = id
				""",
				seatCount, venueId, gradeId);

		jdbcTemplate.update(
				"""
				insert ignore into performance_seats (performance_id, seat_id, status, reserved_by, reserved_at, version, created_at, updated_at)
				select ?, s.id, 'AVAILABLE', null, null, 0, now(), now()
				from seats s
				where s.venue_id = ? and s.seat_no between 1 and ?
				""",
				performanceId, venueId, seatCount);

		Integer seeded = jdbcTemplate.queryForObject(
				"select count(*) from performance_seats where performance_id = ?",
				Integer.class,
				performanceId);
		log.info("loadtest seed completed. performanceId={}, seededSeats={}", performanceId, seeded);
	}
}
