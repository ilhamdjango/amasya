package com.futbol.zire_fk.repository;

import com.futbol.zire_fk.entity.Calculated;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public interface CalculatedRepository extends JpaRepository<Calculated, Long> {

    @Query("SELECT COALESCE(SUM(c.totalCompleted),0) FROM Calculated c WHERE c.createdAt >= :start AND c.createdAt < :end")
    BigDecimal sumTotalCompletedBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

}