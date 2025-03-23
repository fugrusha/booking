package com.booking.unitmanager.model.dto;

import com.booking.unitmanager.model.enums.AccommodationType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UnitReadDTO {

    private Long id;

    private Integer numberOfRooms;

    private AccommodationType accommodationType;

    private Integer floor;

    private BigDecimal baseCost;

    private BigDecimal totalCost;

    private String description;
}
