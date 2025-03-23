package com.booking.unitmanager.service;

import com.booking.unitmanager.dao.BookingRepository;
import com.booking.unitmanager.dao.UnitRepository;
import com.booking.unitmanager.dao.UserRepository;
import com.booking.unitmanager.model.entity.BookingEntity;
import com.booking.unitmanager.model.entity.UnitEntity;
import com.booking.unitmanager.model.entity.UserEntity;
import com.booking.unitmanager.model.enums.AccommodationType;
import com.booking.unitmanager.model.enums.BookingStatus;
import com.booking.unitmanager.service.impl.UnitCacheService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UnitCacheServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UnitCacheService unitCacheService;

    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedisTemplate<String, Long> redisTemplate;

    private static final String AVAILABLE_UNITS_KEY = "available_units";

    private List<UnitEntity> testUnits;
    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        unitRepository.deleteAll();
        userRepository.deleteAll();
        redisTemplate.delete(AVAILABLE_UNITS_KEY);

        testUser = createTestUser();

        testUnits = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            testUnits.add(createTestUnit(i));
        }
    }

    @AfterEach
    void tearDown() {
        bookingRepository.deleteAll();
        unitRepository.deleteAll();
        userRepository.deleteAll();
        redisTemplate.delete(AVAILABLE_UNITS_KEY);
    }

    @Test
    void getAvailableUnitsCount_ShouldReturnCorrectCount() {
        // When
        Long result = unitCacheService.getAvailableUnitsCount();

        // Then
        assertEquals(5L, result);

        // Verify the value was cached in Redis
        Number cachedValue = redisTemplate.opsForValue().get(AVAILABLE_UNITS_KEY);
        assertEquals(5, cachedValue);
    }

    @Test
    void decrementAvailableUnits_ShouldDecrementCount() {
        // Given
        unitCacheService.getAvailableUnitsCount(); // Initialize cache

        // When
        unitCacheService.decrementAvailableUnits();

        // Then
        Long result = unitCacheService.getAvailableUnitsCount();
        assertEquals(4L, result);

        // Verify the value was updated in Redis
        Number cachedValue = redisTemplate.opsForValue().get(AVAILABLE_UNITS_KEY);
        assertEquals(4, cachedValue);
    }

    @Test
    void incrementAvailableUnits_ShouldIncrementCount() {
        // Given
        unitCacheService.getAvailableUnitsCount(); // Initialize cache
        unitCacheService.decrementAvailableUnits(); // Decrement to 4

        // When
        unitCacheService.incrementAvailableUnits();

        // Then
        Long result = unitCacheService.getAvailableUnitsCount();
        assertEquals(5L, result);

        Number cachedValue = redisTemplate.opsForValue().get(AVAILABLE_UNITS_KEY);
        assertEquals(5, cachedValue);
    }

    @Test
    void countAvailableUnits_WithBookings_ShouldReturnCorrectCount() {
        // Given
        // Create bookings for 2 units
        Instant now = Instant.now();
        Instant tomorrow = now.plus(1, ChronoUnit.DAYS);

        for (int i = 0; i < 2; i++) {
            BookingEntity booking = new BookingEntity();
            booking.setUnit(testUnits.get(i));
            booking.setUser(testUser);
            booking.setStartDate(now);
            booking.setEndDate(tomorrow);
            booking.setStatus(BookingStatus.PAID);
            booking.setTotalPrice(new BigDecimal("200.00"));
            bookingRepository.save(booking);
        }

        // When
        Long result = unitCacheService.countAvailableUnits();

        // Then
        assertEquals(3L, result);
    }

    @Test
    void rebuildCache_ShouldUpdateCacheWithCorrectCount() {
        // Given
        // Set an incorrect value in the cache
        redisTemplate.opsForValue().set(AVAILABLE_UNITS_KEY, 10L);

        // When
        unitCacheService.rebuildCache();

        // Then
        Long result = unitCacheService.getAvailableUnitsCount();
        assertEquals(5L, result);

        Number cachedValue = redisTemplate.opsForValue().get(AVAILABLE_UNITS_KEY);
        assertEquals(5, cachedValue);
    }

    @Test
    void initializeCache_ShouldSetCorrectInitialValue() {
        // Given
        redisTemplate.delete(AVAILABLE_UNITS_KEY);

        // When
        unitCacheService.initializeCache();

        // Then
        Number cachedValue = redisTemplate.opsForValue().get(AVAILABLE_UNITS_KEY);
        assertEquals(5, cachedValue);
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

    private UnitEntity createTestUnit(int seed) {
        UnitEntity testUnit = new UnitEntity();
        testUnit.setDescription("Test Description " + seed);
        testUnit.setAccommodationType(AccommodationType.HOME);
        testUnit.setFloor(seed);
        testUnit.setNumberOfRooms(2);
        testUnit.setBaseCost(new BigDecimal("100.00"));
        testUnit.setTotalCost(new BigDecimal("150.00"));
        return unitRepository.save(testUnit);
    }
}
