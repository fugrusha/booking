package com.booking.unitmanager.service.impl;

import com.booking.unitmanager.dao.UnitRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class UnitCacheService {

    private static final String AVAILABLE_UNITS_KEY = "available_units";

    private final RedisTemplate<String, Long> redisTemplate;
    private final UnitRepository unitRepository;

    @PostConstruct
    public void initializeCache() {
        log.info("Initializing unit availability cache");
        rebuildCache();
    }

    public Long getAvailableUnitsCount() {
        Number countObj = redisTemplate.opsForValue().get(AVAILABLE_UNITS_KEY);
        if (countObj != null) {
            return countObj.longValue();
        }

        Long count = countAvailableUnits();
        updateAvailableUnitsCount(count);
        return count;
    }

    public void incrementAvailableUnits() {
        Long currentCount = getAvailableUnitsCount();
        updateAvailableUnitsCount(currentCount + 1);
    }

    public void decrementAvailableUnits() {
        Long currentCount = getAvailableUnitsCount();
        updateAvailableUnitsCount(currentCount - 1);
    }

    private void updateAvailableUnitsCount(Long count) {
        redisTemplate.opsForValue().set(AVAILABLE_UNITS_KEY, count);
    }

    /**
     * Recalculate the number of available units from the database.
     */
    public Long countAvailableUnits() {
        Instant now = Instant.now();
        return unitRepository.countAvailableUnits(now, now.plus(1, ChronoUnit.DAYS));
    }

    /**
     * Rebuild the cache from the database.
     * This can be called to recover from a system crash.
     */
    public void rebuildCache() {
        log.info("Rebuilding unit availability cache");
        Long availableUnits = countAvailableUnits();
        updateAvailableUnitsCount(availableUnits);
        log.info("Cache rebuilt with {} available units", availableUnits);
    }
}
