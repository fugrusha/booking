package com.booking.unitmanager.controller;

import com.booking.unitmanager.exception.EntityNotFoundException;
import com.booking.unitmanager.exception.UnitIsNotAvailableException;
import com.booking.unitmanager.model.dto.BookingCreateDTO;
import com.booking.unitmanager.model.dto.BookingReadDTO;
import com.booking.unitmanager.model.enums.BookingStatus;
import com.booking.unitmanager.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookingService bookingService;

    @Nested
    class CreateBookingTest {
        @Test
        void createBooking_WithValidData_ShouldReturnCreated() throws Exception {
            BookingReadDTO bookingReadDTO = getBookingReadDTO();
            BookingCreateDTO bookingCreateDTO = getBookingCreateDTO();
            when(bookingService.createBooking(any(BookingCreateDTO.class))).thenReturn(bookingReadDTO);

            mockMvc.perform(post("/api/v1/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(bookingCreateDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(bookingReadDTO.getId()))
                    .andExpect(jsonPath("$.unitId").value(bookingReadDTO.getUnitId()))
                    .andExpect(jsonPath("$.userId").value(bookingReadDTO.getUserId()))
                    .andExpect(jsonPath("$.status").value(bookingReadDTO.getStatus().toString()));

            verify(bookingService).createBooking(any(BookingCreateDTO.class));
        }

        @Test
        void createBooking_WithInvalidData_ShouldReturnBadRequest() throws Exception {
            // Create invalid DTO with missing required fields
            BookingCreateDTO invalidDTO = new BookingCreateDTO();
            // Missing unitId, userId, startDate, endDate

            mockMvc.perform(post("/api/v1/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDTO)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void createBooking_WithUnavailableUnit_ShouldReturnConflict() throws Exception {
            BookingCreateDTO bookingCreateDTO = getBookingCreateDTO();
            when(bookingService.createBooking(any(BookingCreateDTO.class)))
                    .thenThrow(new UnitIsNotAvailableException("Unit is not available for the selected dates"));

            mockMvc.perform(post("/api/v1/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(bookingCreateDTO)))
                    .andExpect(status().isBadRequest());

            verify(bookingService).createBooking(any(BookingCreateDTO.class));
        }
    }

    @Nested
    class getBookingByIdTest {

        @Test
        void getBookingById_WithExistingId_ShouldReturnBooking() throws Exception {
            BookingReadDTO bookingReadDTO = getBookingReadDTO();
            when(bookingService.getBooking(1L)).thenReturn(bookingReadDTO);

            mockMvc.perform(get("/api/v1/bookings/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(bookingReadDTO.getId()))
                    .andExpect(jsonPath("$.unitId").value(bookingReadDTO.getUnitId()))
                    .andExpect(jsonPath("$.userId").value(bookingReadDTO.getUserId()))
                    .andExpect(jsonPath("$.status").value(bookingReadDTO.getStatus().toString()));

            verify(bookingService).getBooking(1L);
        }

        @Test
        void getBookingById_WithNonExistingId_ShouldReturnNotFound() throws Exception {
            when(bookingService.getBooking(999L)).thenThrow(new EntityNotFoundException("Booking not found with id: 999"));

            mockMvc.perform(get("/api/v1/bookings/999"))
                    .andExpect(status().isNotFound());

            verify(bookingService).getBooking(999L);
        }
    }

    @Test
    void cancelBooking_WithExistingId_ShouldReturnUpdatedBooking() throws Exception {
        BookingReadDTO bookingReadDTO = getBookingReadDTO();

        BookingReadDTO cancelledBooking = new BookingReadDTO();
        cancelledBooking.setId(bookingReadDTO.getId());
        cancelledBooking.setUnitId(bookingReadDTO.getUnitId());
        cancelledBooking.setUserId(bookingReadDTO.getUserId());
        cancelledBooking.setStartDate(bookingReadDTO.getStartDate());
        cancelledBooking.setEndDate(bookingReadDTO.getEndDate());
        cancelledBooking.setTotalPrice(bookingReadDTO.getTotalPrice());
        cancelledBooking.setStatus(BookingStatus.CANCELLED);
        cancelledBooking.setCreatedAt(bookingReadDTO.getCreatedAt());
        cancelledBooking.setUpdatedAt(Instant.now());
        cancelledBooking.setPaymentDeadline(bookingReadDTO.getPaymentDeadline());

        when(bookingService.cancelBooking(1L)).thenReturn(cancelledBooking);

        mockMvc.perform(post("/api/v1/bookings/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cancelledBooking.getId()))
                .andExpect(jsonPath("$.status").value(cancelledBooking.getStatus().toString()));

        verify(bookingService).cancelBooking(1L);
    }

    @Test
    void findByUserId_ShouldReturnBookingsPage() throws Exception {
        BookingReadDTO bookingReadDTO = getBookingReadDTO();
        Page<BookingReadDTO> bookingsPage = new PageImpl<>(List.of(bookingReadDTO));

        when(bookingService.findByUserId(eq(1L), any(Pageable.class))).thenReturn(bookingsPage);

        mockMvc.perform(get("/api/v1/bookings/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(bookingReadDTO.getId()))
                .andExpect(jsonPath("$.content[0].unitId").value(bookingReadDTO.getUnitId()))
                .andExpect(jsonPath("$.content[0].userId").value(bookingReadDTO.getUserId()));

        verify(bookingService).findByUserId(eq(1L), any(Pageable.class));
    }

    @Test
    void findByUnitId_ShouldReturnBookingsPage() throws Exception {
        BookingReadDTO bookingReadDTO = getBookingReadDTO();
        Page<BookingReadDTO> bookingsPage = new PageImpl<>(List.of(bookingReadDTO));

        when(bookingService.findByUnitId(eq(1L), any(Pageable.class))).thenReturn(bookingsPage);

        mockMvc.perform(get("/api/v1/bookings/unit/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(bookingReadDTO.getId()))
                .andExpect(jsonPath("$.content[0].unitId").value(bookingReadDTO.getUnitId()))
                .andExpect(jsonPath("$.content[0].userId").value(bookingReadDTO.getUserId()));

        verify(bookingService).findByUnitId(eq(1L), any(Pageable.class));
    }

    private BookingReadDTO getBookingReadDTO() {
        Instant now = Instant.now();
        Instant future = now.plusSeconds(86400); // 1 day in the future
        Instant furtherFuture = now.plusSeconds(172800); // 2 days in the future
        BookingReadDTO bookingReadDTO = new BookingReadDTO();
        bookingReadDTO.setId(1L);
        bookingReadDTO.setUnitId(1L);
        bookingReadDTO.setUserId(1L);
        bookingReadDTO.setStartDate(future);
        bookingReadDTO.setEndDate(furtherFuture);
        bookingReadDTO.setTotalPrice(new BigDecimal("100.00"));
        bookingReadDTO.setStatus(BookingStatus.PENDING);
        bookingReadDTO.setCreatedAt(now);
        bookingReadDTO.setUpdatedAt(now);
        bookingReadDTO.setPaymentDeadline(future);
        return bookingReadDTO;
    }

    private BookingCreateDTO getBookingCreateDTO() {
        Instant now = Instant.now();
        Instant future = now.plusSeconds(86400); // 1 day in the future
        Instant furtherFuture = now.plusSeconds(172800); // 2 days in the future

        BookingCreateDTO dto = new BookingCreateDTO();
        dto.setUnitId(1L);
        dto.setUserId(1L);
        dto.setStartDate(future);
        dto.setEndDate(furtherFuture);
        return dto;
    }
}
