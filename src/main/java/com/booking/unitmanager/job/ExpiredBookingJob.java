package com.booking.unitmanager.job;

import com.booking.unitmanager.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpiredBookingJob {

    private final BookingService bookingService;

    @Scheduled(cron = "${job.process.expired.bookings.cron:0 */5 * * * *}")
    public void processExpiredBookings() {
        log.info("Processing expired bookings...");
        bookingService.processExpiredBookings();
        log.info("Expired bookings processed successfully");
    }
}
