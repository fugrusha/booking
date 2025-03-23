package com.booking.unitmanager.model.dto;

import com.booking.unitmanager.model.enums.AccommodationType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record UnitFilter(
        Integer numberOfRooms,
        AccommodationType accommodationType,
        Integer floor,
        BigDecimal minCost,
        BigDecimal maxCost,
        LocalDate startDate,
        LocalDate endDate
) {

    public static UnitFilter empty() {
        return new UnitFilter(null, null, null, null, null, null, null);
    }
}
