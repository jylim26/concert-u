package com.concertu.ticketing.concert.infra;

import com.concertu.ticketing.concert.domain.Concert;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConcertJpaRepository extends JpaRepository<Concert, Long> {}

