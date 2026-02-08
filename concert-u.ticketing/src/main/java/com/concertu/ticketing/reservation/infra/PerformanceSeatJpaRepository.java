package com.concertu.ticketing.reservation.infra;

import com.concertu.ticketing.reservation.api.PerformanceSeatView;
import com.concertu.ticketing.reservation.domain.PerformanceSeat;
import jakarta.persistence.LockModeType;
import java.util.*;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface PerformanceSeatJpaRepository extends JpaRepository<PerformanceSeat, Long> {
	Optional<PerformanceSeat> findByPerformanceIdAndSeatId(Long performanceId, Long seatId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select ps from PerformanceSeat ps where ps.performance.id = :performanceId and ps.seat.id = :seatId")
	Optional<PerformanceSeat> findForUpdateByPerformanceIdAndSeatId(
			@Param("performanceId") Long performanceId,
			@Param("seatId") Long seatId);

	@Query("""
			select new com.concertu.ticketing.reservation.api.PerformanceSeatView(
				s.id, s.seatNo, g.name, g.price, ps.status, ps.reservedBy, ps.reservedAt
			)
			from PerformanceSeat ps
			join ps.seat s
			join s.grade g
			where ps.performance.id = :performanceId
			order by s.seatNo asc
			""")
	List<PerformanceSeatView> findSeatViewsByPerformanceId(@Param("performanceId") Long performanceId);
}
