package com.nikhil.urlshortener.repository;

import com.nikhil.urlshortener.model.Url;
import com.nikhil.urlshortener.model.User;
import com.nikhil.urlshortener.model.Visitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@ActiveProfiles("test")
class VisitorRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private VisitorRepository visitorRepository;

    private Url testUrl;

    @BeforeEach
    void setUp() {
        User user = User.builder()
            .username("testuser")
            .email("test@test.com")
            .password("password")
            .build();
        entityManager.persist(user);

        testUrl = Url.builder()
            .shortCode("abc123")
            .longUrl("https://www.google.com")
            .clickCount(0L)
            .user(user)
            .expiresAt(LocalDateTime.now().plusDays(30))
            .build();
        entityManager.persist(testUrl);

        entityManager.flush();
    }

    @Test
    void shouldCountDistinctIps() {
        // 3 visits from IP "1.1.1.1"
        saveVisitor(testUrl, "1.1.1.1");
        saveVisitor(testUrl, "1.1.1.1");
        saveVisitor(testUrl, "1.1.1.1");

        // 2 visits from IP "2.2.2.2"
        saveVisitor(testUrl, "2.2.2.2");
        saveVisitor(testUrl, "2.2.2.2");

        // 1 visit from IP "3.3.3.3"
        saveVisitor(testUrl, "3.3.3.3");

        entityManager.flush();
        entityManager.clear();

        // Total 6 rows, but only 3 distinct IPs
        long count = visitorRepository.countDistinctVisitorIpByUrl(testUrl);

        // If this equals 3 -> distinct IPs working correctly
        // If this equals 6 -> it's counting rows, not distinct IPs
        System.out.println("Total visitor rows: 6");
        System.out.println("Distinct IP count returned: " + count);

        assertEquals(3, count, "Should count 3 distinct IPs, not 6 total rows");
    }

    @Test
    void shouldReturnZeroWhenNoVisitors() {
        long count = visitorRepository.countDistinctVisitorIpByUrl(testUrl);
        assertEquals(0, count);
    }

    @Test
    void shouldCountSingleVisitor() {
        saveVisitor(testUrl, "1.1.1.1");
        entityManager.flush();
        entityManager.clear();

        long count = visitorRepository.countDistinctVisitorIpByUrl(testUrl);
        assertEquals(1, count);
    }

    private void saveVisitor(Url url, String ip) {
        Visitor visitor = Visitor.builder()
            .url(url)
            .visitorIp(ip)
            .build();
        entityManager.persist(visitor);
    }
}
