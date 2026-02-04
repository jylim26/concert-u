package com.concertu.ticketing.concert.infra;

import com.concertu.ticketing.concert.domain.SeatGrade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatGradeJpaRepository extends JpaRepository<SeatGrade, Long> {}

