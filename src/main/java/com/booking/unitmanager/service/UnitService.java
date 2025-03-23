package com.booking.unitmanager.service;

import com.booking.unitmanager.model.dto.UnitCreateDTO;
import com.booking.unitmanager.model.dto.UnitFilter;
import com.booking.unitmanager.model.dto.UnitReadDTO;
import com.booking.unitmanager.model.dto.UnitUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

public interface UnitService {

    UnitReadDTO createUnit(UnitCreateDTO unit);

    UnitReadDTO getUnit(Long id);

    UnitReadDTO updateUnit(Long id, UnitUpdateDTO unitUpdateDTO);

    void deleteUnit(Long id);

    Page<UnitReadDTO> findByCriteria(UnitFilter unitFilter, Pageable pageable);

    boolean isUnitAvailable(Long unitId, Instant startDate, Instant endDate);

    Long getAvailableUnitsCount();
}
