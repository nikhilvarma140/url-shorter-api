package com.nikhil.urlshortener.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "visitors")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Visitor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "url_id", nullable = false)
    private Url url;

    @Column(nullable = false)
    private String visitorIp;

    @Column(nullable = false)
    private LocalDateTime visitedAt;

    @PrePersist
    protected void onCreate() {
        if (visitedAt == null) {
            visitedAt = LocalDateTime.now();
        }
    }
}
