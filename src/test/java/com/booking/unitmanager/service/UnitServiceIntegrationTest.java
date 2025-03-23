package com.booking.unitmanager.service;

import com.booking.unitmanager.dao.BookingRepository;
import com.booking.unitmanager.dao.UnitRepository;
import com.booking.unitmanager.dao.UserRepository;
import com.booking.unitmanager.exception.EntityNotFoundException;
import com.booking.unitmanager.model.dto.UnitCreateDTO;
import com.booking.unitmanager.model.dto.UnitFilter;
import com.booking.unitmanager.model.dto.UnitReadDTO;
import com.booking.unitmanager.model.dto.UnitUpdateDTO;
import com.booking.unitmanager.model.entity.BookingEntity;
import com.booking.unitmanager.model.entity.UnitEntity;
import com.booking.unitmanager.model.entity.UserEntity;
import com.booking.unitmanager.model.enums.AccommodationType;
import com.booking.unitmanager.model.enums.BookingStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class UnitServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UnitService unitService;

    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        unitRepository.deleteAll();
        userRepository.deleteAll();

        testUser = createTestUser();
    }


    @AfterEach
    void tearDown() {
        bookingRepository.deleteAll();
        unitRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createUnit_WithValidData_ShouldCreateUnit() {
        // When
        UnitCreateDTO unitCreateDTO = getUnitCreateDTO();
        UnitReadDTO result = unitService.createUnit(unitCreateDTO);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(unitCreateDTO.getDescription(), result.getDescription());
        assertEquals(unitCreateDTO.getAccommodationType(), result.getAccommodationType());
        assertEquals(unitCreateDTO.getBaseCost(), result.getBaseCost());
        assertNotNull(result.getTotalCost());

        assertTrue(unitRepository.findById(result.getId()).isPresent());
    }

    @Test
    void getUnit_WithExistingId_ShouldReturnUnit() {
        // Given
        UnitCreateDTO unitCreateDTO = getUnitCreateDTO();
        UnitReadDTO createdUnit = unitService.createUnit(unitCreateDTO);

        // When
        UnitReadDTO result = unitService.getUnit(createdUnit.getId());

        // Then
        assertNotNull(result);
        assertEquals(createdUnit.getId(), result.getId());
        assertEquals(createdUnit.getDescription(), result.getDescription());
    }

    @Test
    void getUnit_WithNonExistingId_ShouldThrowException() {
        // When/Then
        assertThrows(EntityNotFoundException.class, () -> {
            unitService.getUnit(999L);
        });
    }

    @Test
    void updateUnit_WithValidData_ShouldUpdateUnit() {
        // Given
        UnitCreateDTO unitCreateDTO = getUnitCreateDTO();
        UnitReadDTO createdUnit = unitService.createUnit(unitCreateDTO);

        UnitUpdateDTO updateDTO = getUnitUpdateDTO();

        // When
        UnitReadDTO result = unitService.updateUnit(createdUnit.getId(), updateDTO);

        // Then
        assertNotNull(result);
        assertEquals(createdUnit.getId(), result.getId());
        assertEquals(updateDTO.getDescription(), result.getDescription());
        assertEquals(updateDTO.getBaseCost(), result.getBaseCost());
        assertEquals(updateDTO.getNumberOfRooms(), result.getNumberOfRooms());

        UnitEntity updatedEntity = unitRepository.findById(result.getId()).orElseThrow();
        assertEquals(updateDTO.getNumberOfRooms(), updatedEntity.getNumberOfRooms());
        assertEquals(updateDTO.getBaseCost(), updatedEntity.getBaseCost());
        assertEquals(updateDTO.getDescription(), updatedEntity.getDescription());
    }

    @Test
    void deleteUnit_WithExistingId_ShouldDeleteUnit() {
        // Given
        UnitCreateDTO unitCreateDTO = getUnitCreateDTO();
        UnitReadDTO createdUnit = unitService.createUnit(unitCreateDTO);

        // When
        unitService.deleteUnit(createdUnit.getId());

        // Then
        assertFalse(unitRepository.existsById(createdUnit.getId()));
    }

    @Test
    void findAll_ShouldReturnAllUnits() {
        // Given
        UnitCreateDTO unitCreateDTO = getUnitCreateDTO();
        unitService.createUnit(unitCreateDTO);

        UnitCreateDTO anotherUnitDTO = getUnitCreateDTO();
        anotherUnitDTO.setDescription("Another Description");
        anotherUnitDTO.setAccommodationType(AccommodationType.APARTMENTS);
        unitService.createUnit(anotherUnitDTO);

        // When
        UnitFilter unitFilter = UnitFilter.empty();
        Page<UnitReadDTO> result = unitService.findByCriteria(unitFilter, PageRequest.of(0, 10));

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
    }

    @Test
    void findByFilter_ShouldReturnFilteredUnits() {
        // Given
        UnitCreateDTO unitCreateDTO = getUnitCreateDTO();
        unitService.createUnit(unitCreateDTO);

        UnitCreateDTO anotherUnitDTO = new UnitCreateDTO();
        anotherUnitDTO.setDescription("Luxury Description");
        anotherUnitDTO.setAccommodationType(AccommodationType.HOME);
        anotherUnitDTO.setBaseCost(new BigDecimal("300.00"));
        anotherUnitDTO.setFloor(1);
        anotherUnitDTO.setNumberOfRooms(2);
        unitService.createUnit(anotherUnitDTO);

        UnitFilter filter = UnitFilter.builder()
                .accommodationType(AccommodationType.APARTMENTS)
                .build();

        // When
        Page<UnitReadDTO> result = unitService.findByCriteria(filter, PageRequest.of(0, 10));

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(AccommodationType.APARTMENTS, result.getContent().get(0).getAccommodationType());
    }

    @Test
    void isUnitAvailable_WithNoOverlappingBookings_ShouldReturnTrue() {
        // Given
        UnitCreateDTO unitCreateDTO = getUnitCreateDTO();
        UnitReadDTO createdUnit = unitService.createUnit(unitCreateDTO);
        Instant startDate = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant endDate = Instant.now().plus(3, ChronoUnit.DAYS);

        // When
        boolean result = unitService.isUnitAvailable(createdUnit.getId(), startDate, endDate);

        // Then
        assertTrue(result);
    }

    @Test
    void isUnitAvailable_WithOverlappingBookings_ShouldReturnFalse() {
        // Given
        UnitEntity unit = new UnitEntity();
        unit.setNumberOfRooms(2);
        unit.setAccommodationType(AccommodationType.APARTMENTS);
        unit.setFloor(3);
        unit.setBaseCost(new BigDecimal("100.00"));
        unit.setTotalCost(new BigDecimal("150.00"));
        unit.setDescription("Modern apartment with balcony");
        UnitEntity savedUnit = unitRepository.save(unit);

        Instant now = Instant.now();
        Instant startDate = now.plus(1, ChronoUnit.DAYS);
        Instant endDate = now.plus(3, ChronoUnit.DAYS);

        // Create an overlapping booking
        BookingEntity booking = new BookingEntity();
        booking.setUnit(savedUnit);
        booking.setUser(testUser);
        booking.setStartDate(startDate.minus(1, ChronoUnit.DAYS));
        booking.setEndDate(startDate.plus(1, ChronoUnit.DAYS));
        booking.setStatus(BookingStatus.PAID);
        booking.setTotalPrice(new BigDecimal("200.00"));
        bookingRepository.save(booking);

        // When
        boolean result = unitService.isUnitAvailable(unit.getId(), startDate, endDate);

        // Then
        assertFalse(result);
    }

    private UnitCreateDTO getUnitCreateDTO() {
        UnitCreateDTO unitCreateDTO = new UnitCreateDTO();
        unitCreateDTO.setNumberOfRooms(2);
        unitCreateDTO.setAccommodationType(AccommodationType.APARTMENTS);
        unitCreateDTO.setFloor(3);
        unitCreateDTO.setBaseCost(new BigDecimal("150.00"));
        unitCreateDTO.setDescription("Modern apartment with balcony");
        return unitCreateDTO;
    }

    private UnitUpdateDTO getUnitUpdateDTO() {
        UnitUpdateDTO unitUpdateDTO = new UnitUpdateDTO();
        unitUpdateDTO.setNumberOfRooms(2);
        unitUpdateDTO.setAccommodationType(AccommodationType.APARTMENTS);
        unitUpdateDTO.setFloor(3);
        unitUpdateDTO.setBaseCost(new BigDecimal("160.00"));
        unitUpdateDTO.setDescription("Updated modern apartment with balcony");
        return unitUpdateDTO;
    }

    private UserEntity createTestUser() {
        UserEntity testUser = new UserEntity();
        testUser.setUsername("testuser");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPasswordHash("?#1");
        testUser.setEmail("test@example.com");
        return userRepository.save(testUser);
    }
}
