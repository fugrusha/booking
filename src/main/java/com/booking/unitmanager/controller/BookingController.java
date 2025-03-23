package com.booking.unitmanager.controller;

import com.booking.unitmanager.service.BookingService;
import com.booking.unitmanager.model.dto.BookingCreateDTO;
import com.booking.unitmanager.model.dto.BookingReadDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingReadDTO> createBooking(@Valid @RequestBody BookingCreateDTO createDTO) {
        BookingReadDTO booking = bookingService.createBooking(createDTO);
        return new ResponseEntity<>(booking, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingReadDTO> getBookingById(@PathVariable Long id) {
        BookingReadDTO booking = bookingService.getBooking(id);
        return ResponseEntity.ok(booking);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<BookingReadDTO> cancelBooking(@PathVariable Long id) {
        BookingReadDTO booking = bookingService.cancelBooking(id);
        return ResponseEntity.ok(booking);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<BookingReadDTO>> findByUserId(
            @PathVariable Long userId,
            Pageable pageable
    ) {
        Page<BookingReadDTO> bookings = bookingService.findByUserId(userId, pageable);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/unit/{unitId}")
    public ResponseEntity<Page<BookingReadDTO>> findByUnitId(
            @PathVariable Long unitId,
            Pageable pageable
    ) {
        Page<BookingReadDTO> bookings = bookingService.findByUnitId(unitId, pageable);
        return ResponseEntity.ok(bookings);
    }
}
