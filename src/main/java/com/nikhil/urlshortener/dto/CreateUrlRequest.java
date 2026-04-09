package com.nikhil.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record CreateUrlRequest(
    @NotBlank(message = "URL cannot be blank")
    @URL(message = "Must be a valid URL")
    String longUrl,

    Integer expirationDays  // optional, defaults to 365
) {}
