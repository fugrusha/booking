package com.booking.unitmanager.service;

import com.booking.unitmanager.dao.BookingRepository;
import com.booking.unitmanager.dao.PaymentRepository;
import com.booking.unitmanager.dao.UnitRepository;
import com.booking.unitmanager.dao.UserRepository;
import com.booking.unitmanager.exception.EntityNotFoundException;
import com.booking.unitmanager.model.dto.PaymentCreateDTO;
import com.booking.unitmanager.model.dto.PaymentReadDTO;
import com.booking.unitmanager.model.entity.BookingEntity;
import com.booking.unitmanager.model.entity.UnitEntity;
import com.booking.unitmanager.model.entity.UserEntity;
import com.booking.unitmanager.model.enums.AccommodationType;
import com.booking.unitmanager.model.enums.BookingStatus;
import com.booking.unitmanager.model.enums.PaymentStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class PaymentServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private UserRepository userRepository;

    private BookingEntity testBooking;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        bookingRepository.deleteAll();
        unitRepository.deleteAll();
        userRepository.deleteAll();

        UserEntity testUser = createTestUser();
        UnitEntity testUnit = createTestUnit();
        testBooking = createTestBooking(testUnit, testUser);
    }

    @AfterEach
    void tearDown() {
        paymentRepository.deleteAll();
        bookingRepository.deleteAll();
        unitRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createPayment_WithValidData_ShouldCreatePayment() {
        // When
        PaymentCreateDTO paymentCreateDTO = getPaymentCreateDTO();
        PaymentReadDTO result = paymentService.createPayment(paymentCreateDTO);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(testBooking.getId(), result.getBooking().getId());
        assertEquals(paymentCreateDTO.getAmount(), result.getAmount());
        assertEquals(PaymentStatus.COMPLETED, result.getStatus());

        assertTrue(paymentRepository.findById(result.getId()).isPresent());

        BookingEntity updatedBooking = bookingRepository.findById(testBooking.getId()).orElseThrow();
        assertEquals(BookingStatus.PAID, updatedBooking.getStatus());
    }

    @Test
    void createPayment_WithNonExistingBooking_ShouldThrowException() {
        // Given
        PaymentCreateDTO invalidDTO = new PaymentCreateDTO();
        invalidDTO.setBookingId(999L);
        invalidDTO.setAmount(new BigDecimal("200.00"));

        // When/Then
        assertThrows(EntityNotFoundException.class, () -> {
            paymentService.createPayment(invalidDTO);
        });
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

    private BookingEntity createTestBooking(UnitEntity testUnit, UserEntity testUser) {
        BookingEntity testBooking = new BookingEntity();
        testBooking.setUnit(testUnit);
        testBooking.setUser(testUser);
        testBooking.setStartDate(Instant.now().plus(1, ChronoUnit.DAYS));
        testBooking.setEndDate(Instant.now().plus(3, ChronoUnit.DAYS));
        testBooking.setStatus(BookingStatus.PENDING);
        testBooking.setTotalPrice(new BigDecimal("200.00"));
        testBooking.setPaymentDeadline(Instant.now().plus(1, ChronoUnit.HOURS));
        return bookingRepository.save(testBooking);
    }

    private PaymentCreateDTO getPaymentCreateDTO() {
        PaymentCreateDTO paymentCreateDTO = new PaymentCreateDTO();
        paymentCreateDTO.setBookingId(testBooking.getId());
        paymentCreateDTO.setAmount(testBooking.getTotalPrice());
        return paymentCreateDTO;
    }
}
