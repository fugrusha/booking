package com.booking.unitmanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class IllegalStateEntityException extends RuntimeException {
    public IllegalStateEntityException(String message) {
        super(message);
    }
}
