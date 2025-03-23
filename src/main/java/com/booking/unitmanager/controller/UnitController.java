package com.booking.unitmanager.controller;

import com.booking.unitmanager.model.enums.AccommodationType;
import com.booking.unitmanager.service.UnitService;
import com.booking.unitmanager.model.dto.UnitCreateDTO;
import com.booking.unitmanager.model.dto.UnitFilter;
import com.booking.unitmanager.model.dto.UnitReadDTO;
import com.booking.unitmanager.model.dto.UnitUpdateDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/units")
@RequiredArgsConstructor
public class UnitController {

    private final UnitService unitService;

    @PostMapping
    public ResponseEntity<UnitReadDTO> createUnit(@Valid @RequestBody UnitCreateDTO unitCreateDTO) {
        UnitReadDTO unit = unitService.createUnit(unitCreateDTO);
        return new ResponseEntity<>(unit, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UnitReadDTO> getUnitById(@PathVariable Long id) {
        UnitReadDTO unit = unitService.getUnit(id);
        return ResponseEntity.ok(unit);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UnitReadDTO> updateUnit(
            @PathVariable Long id,
            @Valid @RequestBody UnitUpdateDTO unitUpdateDTO
    ) {
        UnitReadDTO unit = unitService.updateUnit(id, unitUpdateDTO);
        return ResponseEntity.ok(unit);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUnit(@PathVariable Long id) {
        unitService.deleteUnit(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<UnitReadDTO>> findByCriteria(
            @RequestParam(required = false) Integer numberOfRooms,
            @RequestParam(required = false) AccommodationType accommodationType,
            @RequestParam(required = false) Integer floor,
            @RequestParam(required = false) BigDecimal minCost,
            @RequestParam(required = false) BigDecimal maxCost,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable
    ) {
        UnitFilter criteria = UnitFilter.builder()
                .numberOfRooms(numberOfRooms)
                .accommodationType(accommodationType)
                .floor(floor)
                .minCost(minCost)
                .maxCost(maxCost)
                .startDate(startDate)
                .endDate(endDate)
                .build();
        Page<UnitReadDTO> units = unitService.findByCriteria(criteria, pageable);
        
        return ResponseEntity.ok(units);
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<Boolean> checkAvailability(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Instant startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Instant endDate
    ) {
        boolean isAvailable = unitService.isUnitAvailable(id, startDate, endDate);
        return ResponseEntity.ok(isAvailable);
    }

    @GetMapping("/available/count")
    public ResponseEntity<Map<String, Long>> getAvailableUnitsCount() {
        Long count = unitService.getAvailableUnitsCount();
        return ResponseEntity.ok(Map.of("availableUnits", count));
    }
}
