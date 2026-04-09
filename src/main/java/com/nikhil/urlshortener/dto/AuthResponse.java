package com.nikhil.urlshortener.dto;

public record AuthResponse(
    String token,
    String username,
    String message
) {}
