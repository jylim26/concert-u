package com.concertu.ticketing.concert.infra;

import com.concertu.ticketing.concert.domain.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatJpaRepository extends JpaRepository<Seat, Long> {}

