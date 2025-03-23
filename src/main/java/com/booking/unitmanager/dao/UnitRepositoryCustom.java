package com.booking.unitmanager.dao;

import com.booking.unitmanager.model.dto.UnitFilter;
import com.booking.unitmanager.model.entity.UnitEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UnitRepositoryCustom {

    Page<UnitEntity> findByCriteriaUsingCriteriaApi(UnitFilter unitFilter, Pageable pageable);
}
