package com.booking.unitmanager.service.impl;

import com.booking.unitmanager.dao.BookingRepository;
import com.booking.unitmanager.dao.UnitRepository;
import com.booking.unitmanager.dao.UserRepository;
import com.booking.unitmanager.exception.EntityNotFoundException;
import com.booking.unitmanager.exception.IllegalStateEntityException;
import com.booking.unitmanager.exception.UnitIsNotAvailableException;
import com.booking.unitmanager.mapper.BookingMapper;
import com.booking.unitmanager.model.dto.BookingCreateDTO;
import com.booking.unitmanager.model.dto.BookingReadDTO;
import com.booking.unitmanager.model.entity.BookingEntity;
import com.booking.unitmanager.model.entity.UnitEntity;
import com.booking.unitmanager.model.entity.UserEntity;
import com.booking.unitmanager.model.enums.BookingStatus;
import com.booking.unitmanager.service.BookingService;
import com.booking.unitmanager.service.UnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UnitRepository unitRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;
    private final UnitService unitService;
    private final UnitCacheService unitCacheService;

    @Value("${booking.payment.threshold.minutes}")
    private Integer paymentThreshold;

    @Override
    @Transactional
    public BookingReadDTO createBooking(BookingCreateDTO bookingCreateDTO) {
        Long unitId = bookingCreateDTO.getUnitId();
        Instant startDate = bookingCreateDTO.getStartDate();
        Instant endDate = bookingCreateDTO.getEndDate();

        if (!unitService.isUnitAvailable(unitId, startDate, endDate)) {
            throw new UnitIsNotAvailableException("Unit is not available for the selected dates");
        }

        UnitEntity unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new EntityNotFoundException("Unit not found with id: " + unitId));

        UserEntity user = userRepository.findById(bookingCreateDTO.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + unitId));

        BookingEntity booking = bookingMapper.toEntity(bookingCreateDTO, unit, user);
        booking.setTotalPrice(calculateTotalPrice(bookingCreateDTO, unit));
        booking.setPaymentDeadline(Instant.now().plus(paymentThreshold, ChronoUnit.MINUTES));
        booking.setStatus(BookingStatus.PENDING);
        BookingEntity savedBooking = bookingRepository.save(booking);

        unitCacheService.decrementAvailableUnits();

        return bookingMapper.toReadDTO(savedBooking);
    }

    private BookingEntity getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public BookingReadDTO getBooking(Long id) {
        BookingEntity booking = getBookingById(id);
        return bookingMapper.toReadDTO(booking);
    }

    @Override
    @Transactional
    public BookingReadDTO cancelBooking(Long id) {
        BookingEntity booking = getBookingById(id);

        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.EXPIRED) {
            throw new IllegalStateEntityException("Booking is already cancelled or expired");
        }

        booking.cancel();
        BookingEntity updatedEntity = bookingRepository.save(booking);

        unitCacheService.incrementAvailableUnits();

        return bookingMapper.toReadDTO(updatedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingReadDTO> findByUserId(Long userId, Pageable pageable) {
        Page<BookingEntity> bookingsPage = bookingRepository.findByUserId(userId, pageable);
        return bookingsPage.map(bookingMapper::toReadDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingReadDTO> findByUnitId(Long unitId, Pageable pageable) {
        Page<BookingEntity> bookingsPage = bookingRepository.findByUnitId(unitId, pageable);
        return bookingsPage.map(bookingMapper::toReadDTO);
    }

    @Override
    @Transactional
    public void processExpiredBookings() {
        List<BookingEntity> expiredBookings = bookingRepository.findExpiredBookings(Instant.now());

        for (BookingEntity booking : expiredBookings) {
            booking.markAsExpired();
            bookingRepository.save(booking);
        }

        unitCacheService.rebuildCache();
    }

    private BigDecimal calculateTotalPrice(
            BookingCreateDTO bookingDTO,
            UnitEntity unitEntity
    ) {
        Instant startDate = bookingDTO.getStartDate();
        Instant endDate = bookingDTO.getEndDate();
        if (unitEntity != null && startDate != null && endDate != null) {
            long days = ChronoUnit.DAYS.between(startDate, endDate);
            return unitEntity.getTotalCost().multiply(BigDecimal.valueOf(days));
        }
        return BigDecimal.ZERO;
    }
}
