package com.vishnu.urlshortener.link.application;

public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException() {
        super("Rate limit exceeded.");
    }
}
