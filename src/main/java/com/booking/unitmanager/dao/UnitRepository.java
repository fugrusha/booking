package com.booking.unitmanager.dao;

import com.booking.unitmanager.model.entity.UnitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface UnitRepository extends JpaRepository<UnitEntity, Long>,
        JpaSpecificationExecutor<UnitEntity>, UnitRepositoryCustom {

    @Query("SELECT COUNT(DISTINCT u) FROM UnitEntity u LEFT JOIN u.bookings b " +
            "WHERE b IS NULL OR b.status IN ('CANCELLED', 'EXPIRED') OR " +
            "NOT(b.startDate < :endDate AND b.endDate > :startDate)")
    Long countAvailableUnits(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);
}
