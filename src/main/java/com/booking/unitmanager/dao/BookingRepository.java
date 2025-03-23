package com.booking.unitmanager.dao;

import com.booking.unitmanager.model.entity.BookingEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, Long> {

    @Query("SELECT b FROM BookingEntity b WHERE b.user.id = :userId")
    Page<BookingEntity> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT b FROM BookingEntity b WHERE b.unit.id = :unitId")
    Page<BookingEntity> findByUnitId(@Param("unitId") Long unitId, Pageable pageable);

    @Query("SELECT b FROM BookingEntity b WHERE b.unit.id = :unitId " +
            "AND b.status IN ('PENDING', 'CONFIRMED', 'PAID') " +
            "AND b.startDate < :endDate AND b.endDate > :startDate")
    List<BookingEntity> findActiveBookingsForUnitInDateRange(
            @Param("unitId") Long unitId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    @Query("SELECT b FROM BookingEntity b WHERE b.status = 'PENDING' " +
            "AND b.paymentDeadline < :now")
    List<BookingEntity> findExpiredBookings(@Param("now") Instant now);
}
