package com.concertu.ticketing.concert.infra;

import com.concertu.ticketing.concert.domain.Performance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerformanceJpaRepository extends JpaRepository<Performance, Long> {}

