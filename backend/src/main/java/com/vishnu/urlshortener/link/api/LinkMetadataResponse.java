package com.vishnu.urlshortener.link.api;

import java.time.Instant;

public record LinkMetadataResponse(
        String shortCode,
        String originalUrl,
        Instant createdAt,
        Instant expiresAt
) {
    public LinkMetadataResponse(String shortCode, String originalUrl, Instant createdAt) {
        this(shortCode, originalUrl, createdAt, null);
    }
}
