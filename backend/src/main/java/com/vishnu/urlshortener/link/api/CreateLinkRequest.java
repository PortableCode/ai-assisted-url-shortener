package com.vishnu.urlshortener.link.api;

import jakarta.validation.constraints.NotBlank;

public record CreateLinkRequest(
        @NotBlank(message = "originalUrl must not be blank")
        String originalUrl
) {
}
