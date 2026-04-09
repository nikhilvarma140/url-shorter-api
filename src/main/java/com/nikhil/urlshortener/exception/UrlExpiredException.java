package com.nikhil.urlshortener.exception;

public class UrlExpiredException extends RuntimeException {
    public UrlExpiredException(String shortCode) {
        super("URL has expired: " + shortCode);
    }
}
