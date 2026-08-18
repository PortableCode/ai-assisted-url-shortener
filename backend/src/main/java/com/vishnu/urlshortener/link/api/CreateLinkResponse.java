package com.vishnu.urlshortener.link.api;

import java.time.Instant;

public record CreateLinkResponse(
        String shortCode,
        String shortUrl,
        String originalUrl,
        Instant createdAt,
        Instant expiresAt
) {
    public CreateLinkResponse(String shortCode, String shortUrl, String originalUrl, Instant createdAt) {
        this(shortCode, shortUrl, originalUrl, createdAt, null);
    }
}
