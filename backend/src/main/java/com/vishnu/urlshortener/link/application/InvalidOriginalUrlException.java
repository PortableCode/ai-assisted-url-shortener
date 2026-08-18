package com.vishnu.urlshortener.link.application;

public class InvalidOriginalUrlException extends RuntimeException {

    public InvalidOriginalUrlException(String message) {
        super(message);
    }

    public InvalidOriginalUrlException(String message, Throwable cause) {
        super(message, cause);
    }
}
