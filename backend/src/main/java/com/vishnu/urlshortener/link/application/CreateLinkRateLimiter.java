package com.vishnu.urlshortener.link.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CreateLinkRateLimiter {

    private static final int MAX_REQUESTS_PER_WINDOW = 20;
    private static final Duration WINDOW_DURATION = Duration.ofSeconds(60);

    private final ConcurrentHashMap<String, WindowState> windows = new ConcurrentHashMap<>();
    private final Clock clock;

    @Autowired
    public CreateLinkRateLimiter() {
        this(Clock.systemUTC());
    }

    CreateLinkRateLimiter(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    public void assertAllowed(String clientId) {
        String normalizedClientId = normalizeClientId(clientId);
        Instant now = clock.instant();

        windows.compute(normalizedClientId, (key, currentState) -> {
            if (currentState == null || currentState.isExpired(now)) {
                return new WindowState(now, 1);
            }
            if (currentState.requestCount() >= MAX_REQUESTS_PER_WINDOW) {
                throw new RateLimitExceededException();
            }
            return currentState.incremented();
        });
    }

    private String normalizeClientId(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            return "unknown";
        }
        return clientId;
    }

    private record WindowState(Instant windowStart, int requestCount) {

        private WindowState incremented() {
            return new WindowState(windowStart, requestCount + 1);
        }

        private boolean isExpired(Instant now) {
            return !windowStart.plus(WINDOW_DURATION).isAfter(now);
        }
    }
}
