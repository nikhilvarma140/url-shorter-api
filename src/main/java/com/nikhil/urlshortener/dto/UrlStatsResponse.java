package com.nikhil.urlshortener.dto;

import com.nikhil.urlshortener.model.Url;

import java.time.LocalDateTime;

public record UrlStatsResponse(
    String shortCode,
    String shortUrl,
    String longUrl,
    Long clickCount,
    LocalDateTime createdAt,
    LocalDateTime expiresAt,
    boolean expired
) {
    public static UrlStatsResponse from(Url url, String baseUrl) {
        return new UrlStatsResponse(
            url.getShortCode(),
            baseUrl + "/" + url.getShortCode(),
            url.getLongUrl(),
            url.getClickCount(),
            url.getCreatedAt(),
            url.getExpiresAt(),
            url.isExpired()
        );
    }
}
