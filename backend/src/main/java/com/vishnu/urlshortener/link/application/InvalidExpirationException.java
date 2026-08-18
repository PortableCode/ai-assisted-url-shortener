package com.vishnu.urlshortener.link.application;

public class InvalidExpirationException extends RuntimeException {

    public InvalidExpirationException(String message) {
        super(message);
    }
}
