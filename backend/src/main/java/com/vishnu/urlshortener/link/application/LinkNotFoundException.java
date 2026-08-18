package com.vishnu.urlshortener.link.application;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class LinkNotFoundException extends RuntimeException {

    public LinkNotFoundException(String shortCode) {
        super("Short code not found: " + shortCode);
    }
}
