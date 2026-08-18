package com.vishnu.urlshortener.link.persistence;

import com.vishnu.urlshortener.link.domain.Link;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
class LinkRepositoryTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgresql = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("url_shortener")
            .withUsername("url_shortener")
            .withPassword("url_shortener");

    @Autowired
    private LinkRepository linkRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void saveAndFindByShortCodePersistsMappedFields() {
        Instant createdAt = Instant.parse("2026-08-17T22:00:00Z");
        Link link = new Link("abc1234", "https://example.com/landing", createdAt);

        Link saved = linkRepository.save(link);
        linkRepository.flush();
        entityManager.clear();

        Optional<Link> found = linkRepository.findByShortCode("abc1234");

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        assertEquals("https://example.com/landing", found.get().getOriginalUrl());
        assertEquals(createdAt, found.get().getCreatedAt());
        assertNull(found.get().getExpiresAt());
        assertEquals(0L, found.get().getClickCount());
        assertNull(found.get().getLastAccessedAt());
    }

    @Test
    void saveAndFindByShortCodePersistsExpiration() {
        Instant createdAt = Instant.parse("2026-08-17T22:00:00Z");
        Instant expiresAt = Instant.parse("2026-08-18T22:00:00Z");
        Link link = new Link("ghi5678", "https://example.com/expiring", createdAt, expiresAt);

        linkRepository.saveAndFlush(link);
        entityManager.clear();

        Optional<Link> found = linkRepository.findByShortCode("ghi5678");

        assertTrue(found.isPresent());
        assertEquals(expiresAt, found.get().getExpiresAt());
    }

    @Test
    void v2MigrationAddsNullableExpirationColumn() {
        Object nullable = entityManager.createNativeQuery("""
                select is_nullable
                from information_schema.columns
                where table_name = 'links'
                  and column_name = 'expires_at'
                """).getSingleResult();

        assertEquals("YES", nullable);
    }

    @Test
    @Transactional
    void deleteByShortCodePhysicallyRemovesRow() {
        Link link = new Link("def5678", "https://example.com/delete", Instant.parse("2026-08-17T22:00:00Z"));
        linkRepository.saveAndFlush(link);
        entityManager.clear();

        int deletedRows = linkRepository.deleteByShortCode("def5678");
        entityManager.clear();

        assertEquals(1, deletedRows);
        assertFalse(linkRepository.findByShortCode("def5678").isPresent());
    }
}
