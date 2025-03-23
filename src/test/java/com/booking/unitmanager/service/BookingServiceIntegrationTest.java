package com.booking.unitmanager.service;

import com.booking.unitmanager.dao.BookingRepository;
import com.booking.unitmanager.dao.UnitRepository;
import com.booking.unitmanager.dao.UserRepository;
import com.booking.unitmanager.exception.EntityNotFoundException;
import com.booking.unitmanager.exception.UnitIsNotAvailableException;
import com.booking.unitmanager.model.dto.BookingCreateDTO;
import com.booking.unitmanager.model.dto.BookingReadDTO;
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

class BookingServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private UserRepository userRepository;

    private UnitEntity testUnit;
    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        unitRepository.deleteAll();
        userRepository.deleteAll();

        testUser = createTestUser();
        testUnit = createTestUnit();
    }

    @AfterEach
    void tearDown() {
        bookingRepository.deleteAll();
        unitRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createBooking_WithValidData_ShouldCreateBooking() {
        // When
        BookingCreateDTO createDTO = getBookingCreateDTO();

        BookingReadDTO result = bookingService.createBooking(createDTO);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(testUnit.getId(), result.getUnitId());
        assertEquals(testUser.getId(), result.getUserId());
        assertEquals(BookingStatus.PENDING, result.getStatus());
        assertEquals(createDTO.getStartDate(), result.getStartDate());
        assertEquals(createDTO.getEndDate(), result.getEndDate());

        assertTrue(bookingRepository.findById(result.getId()).isPresent());
    }

    @Test
    void createBooking_WithOverlappingDates_ShouldThrowException() {
        // Given
        Instant startDate = Instant.now().plus(1, ChronoUnit.DAYS);

        // Create an existing booking for the same unit and overlapping dates
        BookingEntity existingBooking = new BookingEntity();
        existingBooking.setUnit(testUnit);
        existingBooking.setUser(testUser);
        existingBooking.setStartDate(startDate.minus(1, ChronoUnit.DAYS));
        existingBooking.setEndDate(startDate.plus(1, ChronoUnit.DAYS));
        existingBooking.setStatus(BookingStatus.PAID);
        existingBooking.setTotalPrice(new BigDecimal("200.00"));
        bookingRepository.save(existingBooking);

        // When/Then
        BookingCreateDTO bookingCreateDTO = getBookingCreateDTO();
        assertThrows(UnitIsNotAvailableException.class, () -> {
            bookingService.createBooking(bookingCreateDTO);
        });
    }

    @Test
    void getBooking_WithExistingId_ShouldReturnBooking() {
        // Given
        BookingCreateDTO bookingCreateDTO = getBookingCreateDTO();
        BookingReadDTO createdBooking = bookingService.createBooking(bookingCreateDTO);

        // When
        BookingReadDTO result = bookingService.getBooking(createdBooking.getId());

        // Then
        assertNotNull(result);
        assertEquals(createdBooking.getId(), result.getId());
        assertEquals(createdBooking.getUnitId(), result.getUnitId());
        assertEquals(createdBooking.getUserId(), result.getUserId());
    }

    @Test
    void getBooking_WithNonExistingId_ShouldThrowException() {
        // When/Then
        assertThrows(EntityNotFoundException.class, () -> {
            bookingService.getBooking(999L);
        });
    }

    @Test
    void cancelBooking_WithExistingId_ShouldCancelBooking() {
        // Given
        BookingCreateDTO bookingCreateDTO = getBookingCreateDTO();
        BookingReadDTO createdBooking = bookingService.createBooking(bookingCreateDTO);

        // When
        BookingReadDTO result = bookingService.cancelBooking(createdBooking.getId());

        // Then
        assertNotNull(result);
        assertEquals(createdBooking.getId(), result.getId());
        assertEquals(BookingStatus.CANCELLED, result.getStatus());

        // Verify booking was updated in database
        BookingEntity updatedEntity = bookingRepository.findById(result.getId()).orElseThrow();
        assertEquals(BookingStatus.CANCELLED, updatedEntity.getStatus());
    }

    @Test
    void findByUserId_ShouldReturnUserBookings() {
        // Given
        BookingCreateDTO bookingCreateDTO = getBookingCreateDTO();
        bookingService.createBooking(bookingCreateDTO);

        // Create another booking for the same user
        Instant startDate = bookingCreateDTO.getStartDate().plus(1, ChronoUnit.DAYS);
        Instant endDate = bookingCreateDTO.getEndDate().plus(3, ChronoUnit.DAYS);

        BookingCreateDTO anotherBookingDTO = new BookingCreateDTO();
        anotherBookingDTO.setUnitId(testUnit.getId());
        anotherBookingDTO.setUserId(testUser.getId());
        anotherBookingDTO.setStartDate(startDate.plus(5, ChronoUnit.DAYS));
        anotherBookingDTO.setEndDate(endDate.plus(5, ChronoUnit.DAYS));
        bookingService.createBooking(anotherBookingDTO);

        // When
        Page<BookingReadDTO> result = bookingService.findByUserId(testUser.getId(), PageRequest.of(0, 10));

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(testUser.getId(), result.getContent().get(0).getUserId());
        assertEquals(testUser.getId(), result.getContent().get(1).getUserId());
    }

    @Test
    void findByUnitId_ShouldReturnUnitBookings() {
        // Given
        BookingCreateDTO bookingCreateDTO = getBookingCreateDTO();
        bookingService.createBooking(bookingCreateDTO);

        // When
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<BookingReadDTO> result = bookingService.findByUnitId(testUnit.getId(), pageRequest);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testUnit.getId(), result.getContent().get(0).getUnitId());
    }

    @Test
    void processExpiredBookings_ShouldMarkExpiredBookings() {
        // Given
        Instant now = Instant.now();
        Instant startDate = now.plus(1, ChronoUnit.DAYS);
        Instant endDate = now.plus(3, ChronoUnit.DAYS);
        // Create a booking with payment deadline in the past
        BookingEntity expiredBooking = new BookingEntity();
        expiredBooking.setUnit(testUnit);
        expiredBooking.setUser(testUser);
        expiredBooking.setStartDate(startDate);
        expiredBooking.setEndDate(endDate);
        expiredBooking.setStatus(BookingStatus.PENDING);
        expiredBooking.setTotalPrice(new BigDecimal("200.00"));
        expiredBooking.setPaymentDeadline(Instant.now().minus(1, ChronoUnit.HOURS));
        bookingRepository.save(expiredBooking);

        // When
        bookingService.processExpiredBookings();

        // Then
        BookingEntity updatedBooking = bookingRepository.findById(expiredBooking.getId()).orElseThrow();
        assertEquals(BookingStatus.EXPIRED, updatedBooking.getStatus());
    }

    private BookingCreateDTO getBookingCreateDTO() {
        Instant now = Instant.now();
        Instant startDate = now.plus(1, ChronoUnit.DAYS);
        Instant endDate = now.plus(3, ChronoUnit.DAYS);

        BookingCreateDTO validBookingCreateDTO = new BookingCreateDTO();
        validBookingCreateDTO.setUnitId(testUnit.getId());
        validBookingCreateDTO.setUserId(testUser.getId());
        validBookingCreateDTO.setStartDate(startDate);
        validBookingCreateDTO.setEndDate(endDate);
        return validBookingCreateDTO;
    }

    private UnitEntity createTestUnit() {
        UnitEntity testUnit = new UnitEntity();
        testUnit.setDescription("Test Description");
        testUnit.setAccommodationType(AccommodationType.HOME);
        testUnit.setFloor(1);
        testUnit.setNumberOfRooms(2);
        testUnit.setBaseCost(new BigDecimal("100.00"));
        testUnit.setTotalCost(new BigDecimal("150.00"));
        return unitRepository.save(testUnit);
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
