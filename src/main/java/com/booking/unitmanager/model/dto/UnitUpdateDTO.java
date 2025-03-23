package com.booking.unitmanager.model.dto;

import com.booking.unitmanager.model.enums.AccommodationType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UnitUpdateDTO {

    @NotNull(message = "Number of rooms is required")
    @Min(value = 1, message = "Number of rooms must be at least 1")
    private Integer numberOfRooms;

    @NotNull(message = "Accommodation type is required")
    private AccommodationType accommodationType;

    @NotNull(message = "Floor is required")
    @Min(value = 0, message = "Floor must be at least 0")
    private Integer floor;

    @NotNull(message = "Base cost is required")
    @Positive(message = "Base cost must be positive")
    private BigDecimal baseCost;

    private String description;
}
