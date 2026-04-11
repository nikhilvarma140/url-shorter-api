package com.nikhil.urlshortener.controller;

import com.nikhil.urlshortener.dto.CreateUrlRequest;
import com.nikhil.urlshortener.dto.UrlResponse;
import com.nikhil.urlshortener.dto.UrlStatsResponse;
import com.nikhil.urlshortener.model.Url;
import com.nikhil.urlshortener.model.User;
import com.nikhil.urlshortener.repository.UserRepository;
import com.nikhil.urlshortener.repository.VisitorRepository;
import com.nikhil.urlshortener.service.UrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "URL Shortener", description = "URL shortening and redirection APIs")
public class UrlController {

    private final UrlService urlService;
    private final UserRepository userRepository;
    private final VisitorRepository visitorRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    @PostMapping("/api/v1/urls")
    @Operation(summary = "Create a short URL")
    public ResponseEntity<UrlResponse> createShortUrl(
        @Valid @RequestBody CreateUrlRequest request,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = userRepository.findByUsername(userDetails.getUsername())
            .orElseThrow();

        Url url = urlService.createShortUrl(request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(UrlResponse.from(url, baseUrl));
    }

    @GetMapping("/{shortCode}")
    @Operation(summary = "Redirect to original URL")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode, HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        String ip = forwarded != null ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
        String longUrl = urlService.resolve(shortCode);
        urlService.recordClick(shortCode,ip);

        return ResponseEntity.status(HttpStatus.FOUND)
            .location(URI.create(longUrl))
            .build();
    }

    @GetMapping("/api/v1/urls/{shortCode}/stats")
    @Operation(summary = "Get URL click statistics")
    public ResponseEntity<UrlStatsResponse> getStats(@PathVariable String shortCode) {
        Url url = urlService.getUrlStats(shortCode);
        long count = visitorRepository.countDistinctVisitorIpByUrl(url);
        return ResponseEntity.ok(UrlStatsResponse.from(url, baseUrl,count));
    }

    @GetMapping("/api/v1/urls/my")
    @Operation(summary = "Get all URLs created by the authenticated user")
    public ResponseEntity<List<UrlResponse>> getMyUrls(
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = userRepository.findByUsername(userDetails.getUsername())
            .orElseThrow();

        List<UrlResponse> urls = urlService.getUserUrls(user.getId())
            .stream()
            .map(url -> UrlResponse.from(url, baseUrl))
            .toList();

        return ResponseEntity.ok(urls);
    }
}
