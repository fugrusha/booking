package com.booking.unitmanager.service;

import com.booking.unitmanager.model.dto.BookingCreateDTO;
import com.booking.unitmanager.model.dto.BookingReadDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface BookingService {

    BookingReadDTO createBooking(BookingCreateDTO createDTO);

    BookingReadDTO getBooking(Long id);

    BookingReadDTO cancelBooking(Long id);

    Page<BookingReadDTO> findByUserId(Long userId, Pageable pageable);

    Page<BookingReadDTO> findByUnitId(Long unitId, Pageable pageable);

    void processExpiredBookings();
}
