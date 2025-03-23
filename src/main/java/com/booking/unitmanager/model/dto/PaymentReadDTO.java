package com.booking.unitmanager.model.dto;

import com.booking.unitmanager.model.enums.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentReadDTO {

    private Long id;

    private BookingReadDTO booking;

    private BigDecimal amount;

    private PaymentStatus status;
}
