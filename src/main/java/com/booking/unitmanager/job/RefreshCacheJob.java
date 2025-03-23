package com.booking.unitmanager.job;

import com.booking.unitmanager.service.impl.UnitCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshCacheJob {

    private final UnitCacheService unitCacheService;

    @Scheduled(cron = "${job.refresh.unit.cache.cron:0 0 * * * *}")
    public void refresh() {
        log.info("Start refreshing cache...");
        unitCacheService.rebuildCache();
        log.info("Finished refreshing cache");
    }
}
