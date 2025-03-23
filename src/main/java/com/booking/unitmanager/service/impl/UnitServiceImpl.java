package com.booking.unitmanager.service.impl;

import com.booking.unitmanager.exception.EntityNotFoundException;
import com.booking.unitmanager.model.entity.BookingEntity;
import com.booking.unitmanager.dao.BookingRepository;
import com.booking.unitmanager.mapper.UnitMapper;
import com.booking.unitmanager.service.UnitService;
import com.booking.unitmanager.model.entity.UnitEntity;
import com.booking.unitmanager.dao.UnitRepository;
import com.booking.unitmanager.model.dto.UnitCreateDTO;
import com.booking.unitmanager.model.dto.UnitFilter;
import com.booking.unitmanager.model.dto.UnitReadDTO;
import com.booking.unitmanager.model.dto.UnitUpdateDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
class UnitServiceImpl implements UnitService {

    private final UnitRepository unitRepository;
    private final BookingRepository bookingRepository;
    private final UnitMapper unitMapper;
    private final UnitCacheService unitCacheService;

    @Value("${booking.system.markup}")
    private BigDecimal systemMarkup;

    @Override
    @Transactional
    public UnitReadDTO createUnit(UnitCreateDTO unitCreateDTO) {
        UnitEntity unit = unitMapper.toEntity(unitCreateDTO);
        unit.setTotalCost(calculateTotalCost(unitCreateDTO.getBaseCost()));
        UnitEntity createdUnit = unitRepository.save(unit);

        unitCacheService.incrementAvailableUnits();

        return unitMapper.toReadDTO(createdUnit);
    }

    @Override
    @Transactional(readOnly = true)
    public UnitReadDTO getUnit(Long id) {
        return unitMapper.toReadDTO(getUnitById(id));
    }

    @Override
    @Transactional
    public UnitReadDTO updateUnit(Long id, UnitUpdateDTO unitUpdateDTO) {
        UnitEntity existingUnit = getUnitById(id);
        unitMapper.updateEntityFromDTO(unitUpdateDTO, existingUnit);
        existingUnit.setTotalCost(calculateTotalCost(unitUpdateDTO.getBaseCost()));

        UnitEntity updatedUnit = unitRepository.save(existingUnit);

        return unitMapper.toReadDTO(updatedUnit);
    }

    @Override
    @Transactional
    public void deleteUnit(Long id) {
        UnitEntity unit = getUnitById(id);
        unitRepository.delete(unit);
        unitCacheService.decrementAvailableUnits();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UnitReadDTO> findByCriteria(UnitFilter filter, Pageable pageable) {
        Page<UnitEntity> unitsPage = unitRepository
                .findByCriteriaUsingCriteriaApi(filter, pageable);
        return unitsPage.map(unitMapper::toReadDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUnitAvailable(Long unitId, Instant startDate, Instant endDate) {
        List<BookingEntity> activeBookings = bookingRepository.findActiveBookingsForUnitInDateRange(
                unitId, startDate, endDate);

        return activeBookings.isEmpty();
    }

    @Override
    @Transactional(readOnly = true)
    public Long getAvailableUnitsCount() {
        return unitCacheService.getAvailableUnitsCount();
    }

    private UnitEntity getUnitById(Long id) {
        return unitRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Unit not found with id: " + id));
    }

    private BigDecimal calculateTotalCost(BigDecimal baseCost) {
        if (baseCost == null) {
            return BigDecimal.ZERO;
        }
        return baseCost.multiply(systemMarkup);
    }
}
