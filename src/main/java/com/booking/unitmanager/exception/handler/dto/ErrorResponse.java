package com.booking.unitmanager.exception.handler.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class ErrorResponse {

    private final int status;
    private final String message;
    private final Instant timestamp;
}
