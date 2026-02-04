package com.concertu.ticketing.concert.infra;

import com.concertu.ticketing.concert.domain.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VenueJpaRepository extends JpaRepository<Venue, Long> {}

