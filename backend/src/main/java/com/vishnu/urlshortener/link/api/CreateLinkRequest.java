package com.vishnu.urlshortener.link.api;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public record CreateLinkRequest(
        @NotBlank(message = "originalUrl must not be blank")
        String originalUrl,
        Instant expiresAt
) {
    public CreateLinkRequest(String originalUrl) {
        this(originalUrl, null);
    }
}
