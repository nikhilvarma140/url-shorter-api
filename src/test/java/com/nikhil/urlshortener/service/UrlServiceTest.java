package com.nikhil.urlshortener.service;

import com.nikhil.urlshortener.dto.CreateUrlRequest;
import com.nikhil.urlshortener.exception.UrlExpiredException;
import com.nikhil.urlshortener.exception.UrlNotFoundException;
import com.nikhil.urlshortener.model.Url;
import com.nikhil.urlshortener.model.User;
import com.nikhil.urlshortener.repository.UrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

    @Mock
    private UrlRepository urlRepository;

    @InjectMocks
    private UrlService urlService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .id(1L)
            .username("nikhil")
            .email("nikhil@test.com")
            .password("encoded")
            .build();
    }

    @Test
    void shouldCreateShortUrl() {
        CreateUrlRequest request = new CreateUrlRequest("https://www.google.com", null);

        when(urlRepository.findByLongUrlAndUserId("https://www.google.com", 1L))
            .thenReturn(Optional.empty());
        when(urlRepository.save(any(Url.class)))
            .thenAnswer(invocation -> {
                Url url = invocation.getArgument(0);
                url.setId(100L);
                return url;
            });

        Url result = urlService.createShortUrl(request, testUser);

        assertNotNull(result);
        assertNotNull(result.getShortCode());
        assertEquals("https://www.google.com", result.getLongUrl());
        verify(urlRepository, times(2)).save(any(Url.class));
    }

    @Test
    void shouldReturnExistingUrlIfAlreadyShortened() {
        Url existing = Url.builder()
            .id(1L)
            .shortCode("abc")
            .longUrl("https://www.google.com")
            .expiresAt(LocalDateTime.now().plusDays(30))
            .clickCount(5L)
            .build();

        when(urlRepository.findByLongUrlAndUserId("https://www.google.com", 1L))
            .thenReturn(Optional.of(existing));

        CreateUrlRequest request = new CreateUrlRequest("https://www.google.com", null);
        Url result = urlService.createShortUrl(request, testUser);

        assertEquals("abc", result.getShortCode());
        verify(urlRepository, never()).save(any());
    }

    @Test
    void shouldResolveShortCode() {
        Url url = Url.builder()
            .shortCode("abc")
            .longUrl("https://www.google.com")
            .expiresAt(LocalDateTime.now().plusDays(30))
            .build();

        when(urlRepository.findByShortCode("abc")).thenReturn(Optional.of(url));

        String result = urlService.resolve("abc");
        assertEquals("https://www.google.com", result);
    }

    @Test
    void shouldThrowWhenShortCodeNotFound() {
        when(urlRepository.findByShortCode("xyz")).thenReturn(Optional.empty());

        assertThrows(UrlNotFoundException.class, () -> urlService.resolve("xyz"));
    }

    @Test
    void shouldThrowWhenUrlExpired() {
        Url url = Url.builder()
            .shortCode("old")
            .longUrl("https://www.google.com")
            .expiresAt(LocalDateTime.now().minusDays(1))
            .build();

        when(urlRepository.findByShortCode("old")).thenReturn(Optional.of(url));

        assertThrows(UrlExpiredException.class, () -> urlService.resolve("old"));
    }

    @Test
    void shouldRecordClick() {
        Url url = Url.builder()
            .shortCode("abc")
            .longUrl("https://www.google.com")
            .clickCount(5L)
            .build();

        when(urlRepository.findByShortCode("abc")).thenReturn(Optional.of(url));

        urlService.recordClick("abc");

        assertEquals(6L, url.getClickCount());
        verify(urlRepository).save(url);
    }
}
