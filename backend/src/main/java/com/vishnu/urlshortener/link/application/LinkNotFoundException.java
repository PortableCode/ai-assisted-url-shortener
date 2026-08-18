package com.vishnu.urlshortener.link.application;

public class LinkNotFoundException extends RuntimeException {

    public LinkNotFoundException(String shortCode) {
        super("Short code not found: " + shortCode);
    }
}
