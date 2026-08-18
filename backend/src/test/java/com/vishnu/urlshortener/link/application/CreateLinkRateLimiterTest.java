package com.vishnu.urlshortener.link.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreateLinkRateLimiterTest {

    private MutableClock clock;
    private CreateLinkRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-08-18T14:00:00Z"), ZoneId.of("UTC"));
        rateLimiter = new CreateLinkRateLimiter(clock);
    }

    @Test
    void allowsRequestsThroughConfiguredThreshold() {
        for (int i = 0; i < 20; i++) {
            assertDoesNotThrow(() -> rateLimiter.assertAllowed("203.0.113.10"));
        }
    }

    @Test
    void rejectsTwentyFirstRequestInSameWindow() {
        for (int i = 0; i < 20; i++) {
            rateLimiter.assertAllowed("203.0.113.10");
        }

        assertThrows(RateLimitExceededException.class, () -> rateLimiter.assertAllowed("203.0.113.10"));
    }

    @Test
    void keepsCountersIndependentPerClient() {
        for (int i = 0; i < 20; i++) {
            rateLimiter.assertAllowed("203.0.113.10");
        }

        assertDoesNotThrow(() -> rateLimiter.assertAllowed("198.51.100.5"));
        assertThrows(RateLimitExceededException.class, () -> rateLimiter.assertAllowed("203.0.113.10"));
    }

    @Test
    void resetsAllowanceAfterSixtySecondWindowExpires() {
        for (int i = 0; i < 20; i++) {
            rateLimiter.assertAllowed("203.0.113.10");
        }

        clock.setInstant(Instant.parse("2026-08-18T14:01:00Z"));

        assertDoesNotThrow(() -> rateLimiter.assertAllowed("203.0.113.10"));
    }

    private static final class MutableClock extends Clock {

        private final ZoneId zoneId;
        private final AtomicReference<Instant> currentInstant;

        private MutableClock(Instant initialInstant, ZoneId zoneId) {
            this.zoneId = zoneId;
            this.currentInstant = new AtomicReference<>(initialInstant);
        }

        private void setInstant(Instant instant) {
            currentInstant.set(instant);
        }

        @Override
        public ZoneId getZone() {
            return zoneId;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new MutableClock(currentInstant.get(), zone);
        }

        @Override
        public Instant instant() {
            return currentInstant.get();
        }
    }
}
