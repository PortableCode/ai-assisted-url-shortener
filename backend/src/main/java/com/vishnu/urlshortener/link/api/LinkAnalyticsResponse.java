package com.vishnu.urlshortener.link.api;

import java.time.Instant;

public record LinkAnalyticsResponse(
        String shortCode,
        long clickCount,
        Instant lastAccessedAt
) {
}
