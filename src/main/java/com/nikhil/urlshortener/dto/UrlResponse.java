package com.nikhil.urlshortener.dto;

import com.nikhil.urlshortener.model.Url;

import java.time.LocalDateTime;

public record UrlResponse(
    String shortCode,
    String shortUrl,
    String longUrl,
    Long clickCount,
    LocalDateTime createdAt,
    LocalDateTime expiresAt
) {
    public static UrlResponse from(Url url, String baseUrl) {
        return new UrlResponse(
            url.getShortCode(),
            baseUrl + "/" + url.getShortCode(),
            url.getLongUrl(),
            url.getClickCount(),
            url.getCreatedAt(),
            url.getExpiresAt()
        );
    }
}
