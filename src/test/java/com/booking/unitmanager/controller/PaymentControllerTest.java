package com.booking.unitmanager.controller;

import com.booking.unitmanager.exception.EntityNotFoundException;
import com.booking.unitmanager.model.dto.BookingReadDTO;
import com.booking.unitmanager.model.dto.PaymentCreateDTO;
import com.booking.unitmanager.model.dto.PaymentReadDTO;
import com.booking.unitmanager.model.enums.PaymentStatus;
import com.booking.unitmanager.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    void processPayment_WithValidData_ShouldReturnOk() throws Exception {
        PaymentReadDTO paymentReadDTO = getPaymentReadDTO();
        PaymentCreateDTO paymentCreateDTO = getPaymentCreateDTO();
        when(paymentService.createPayment(any(PaymentCreateDTO.class))).thenReturn(paymentReadDTO);

        mockMvc.perform(post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentCreateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(paymentReadDTO.getId()))
                .andExpect(jsonPath("$.amount").value(paymentReadDTO.getAmount().doubleValue()))
                .andExpect(jsonPath("$.status").value(paymentReadDTO.getStatus().toString()))
                .andExpect(jsonPath("$.booking.id").value(paymentReadDTO.getBooking().getId()));

        verify(paymentService).createPayment(any(PaymentCreateDTO.class));
    }

    @Test
    void processPayment_WithInvalidData_ShouldHandleError() throws Exception {
        PaymentCreateDTO invalidDTO = new PaymentCreateDTO();

        mockMvc.perform(post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void processPayment_WithNonExistingBooking_ShouldReturnNotFound() throws Exception {
        PaymentCreateDTO invalidBookingDTO = new PaymentCreateDTO();
        invalidBookingDTO.setBookingId(999L);
        invalidBookingDTO.setAmount(new BigDecimal("100.00"));

        when(paymentService.createPayment(any(PaymentCreateDTO.class)))
                .thenThrow(new EntityNotFoundException("Booking not found with id: 999"));

        mockMvc.perform(post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidBookingDTO)))
                .andExpect(status().isNotFound());

        verify(paymentService).createPayment(any(PaymentCreateDTO.class));
    }

    private PaymentReadDTO getPaymentReadDTO() {
        BookingReadDTO bookingReadDTO = new BookingReadDTO();
        bookingReadDTO.setId(1L);

        PaymentReadDTO paymentReadDTO = new PaymentReadDTO();
        paymentReadDTO.setId(1L);
        paymentReadDTO.setBooking(bookingReadDTO);
        paymentReadDTO.setAmount(new BigDecimal("100.00"));
        paymentReadDTO.setStatus(PaymentStatus.COMPLETED);
        return paymentReadDTO;
    }

    private PaymentCreateDTO getPaymentCreateDTO() {
        PaymentCreateDTO paymentCreateDTO = new PaymentCreateDTO();
        paymentCreateDTO.setBookingId(1L);
        paymentCreateDTO.setAmount(new BigDecimal("100.00"));
        return paymentCreateDTO;
    }
}
