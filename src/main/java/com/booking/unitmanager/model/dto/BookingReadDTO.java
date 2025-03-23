package com.booking.unitmanager.model.dto;

import com.booking.unitmanager.model.enums.BookingStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class BookingReadDTO {

    private Long id;

    private Long unitId;

    private Long userId;

    private Instant startDate;

    private Instant endDate;

    private BigDecimal totalPrice;

    private BookingStatus status;

    private Instant createdAt;

    private Instant updatedAt;

    private Instant paymentDeadline;
}
