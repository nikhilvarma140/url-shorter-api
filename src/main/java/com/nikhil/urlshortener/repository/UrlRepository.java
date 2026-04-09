package com.nikhil.urlshortener.repository;

import com.nikhil.urlshortener.model.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<Url, Long> {

    Optional<Url> findByShortCode(String shortCode);

    Optional<Url> findByLongUrlAndUserId(String longUrl, Long userId);

    List<Url> findByUserIdOrderByCreatedAtDesc(Long userId);

    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
