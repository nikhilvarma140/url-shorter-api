package com.nikhil.urlshortener.service;

import com.nikhil.urlshortener.dto.CreateUrlRequest;
import com.nikhil.urlshortener.exception.UrlExpiredException;
import com.nikhil.urlshortener.exception.UrlNotFoundException;
import com.nikhil.urlshortener.model.Url;
import com.nikhil.urlshortener.model.User;
import com.nikhil.urlshortener.repository.UrlRepository;
import com.nikhil.urlshortener.util.Base62Encoder;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository urlRepository;

    @Transactional
    public Url createShortUrl(CreateUrlRequest request, User user) {
        // Check if user already shortened this URL
        Optional<Url> existing = urlRepository.findByLongUrlAndUserId(request.longUrl(), user.getId());
        if (existing.isPresent() && !existing.get().isExpired()) {
            return existing.get();
        }

        int expirationDays = request.expirationDays() != null ? request.expirationDays() : 365;

        // Save first to get auto-generated ID
        Url url = Url.builder()
            .longUrl(request.longUrl())
            .shortCode("temp")  // placeholder
            .user(user)
            .clickCount(0L)
            .createdAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusDays(expirationDays))
            .build();

        url = urlRepository.save(url);

        // Generate short code from ID using Base62
        String shortCode = Base62Encoder.encode(url.getId());
        url.setShortCode(shortCode);

        return urlRepository.save(url);
    }

    @Transactional(readOnly = true)
    public String resolve(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
            .orElseThrow(() -> new UrlNotFoundException(shortCode));

        if (url.isExpired()) {
            throw new UrlExpiredException(shortCode);
        }

        return url.getLongUrl();
    }

    @Transactional
    public void recordClick(String shortCode) {
        urlRepository.findByShortCode(shortCode)
            .ifPresent(url -> {
                url.incrementClickCount();
                urlRepository.save(url);
            });
    }

    @Transactional(readOnly = true)
    public Url getUrlStats(String shortCode) {
        return urlRepository.findByShortCode(shortCode)
            .orElseThrow(() -> new UrlNotFoundException(shortCode));
    }

    @Transactional(readOnly = true)
    public List<Url> getUserUrls(Long userId) {
        return urlRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    @Scheduled(cron = "0 0 2 * * *")  // Run daily at 2 AM
    public void cleanupExpiredUrls() {
        urlRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}
