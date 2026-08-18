package com.vishnu.urlshortener.link.application;

public class LinkExpiredException extends RuntimeException {

    public LinkExpiredException(String shortCode) {
        super("Short code has expired: " + shortCode);
    }
}
