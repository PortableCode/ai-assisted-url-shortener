package com.vishnu.urlshortener.link.application;

import com.vishnu.urlshortener.link.domain.Link;
import com.vishnu.urlshortener.link.infrastructure.ShortCodeGenerator;
import com.vishnu.urlshortener.link.persistence.LinkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LinkServiceTest {

    @Mock
    private LinkRepository linkRepository;

    @Mock
    private ShortCodeGenerator shortCodeGenerator;

    private LinkService linkService;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-08-17T23:30:00Z"), ZoneOffset.UTC);
        linkService = new LinkService(linkRepository, shortCodeGenerator, fixedClock);
    }

    @Test
    void createLinkPersistsValidUrl() {
        when(shortCodeGenerator.generateCandidate()).thenReturn("abc1234");
        when(linkRepository.saveAndFlush(any(Link.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Link created = linkService.createLink("https://example.com/landing");

        ArgumentCaptor<Link> captor = ArgumentCaptor.forClass(Link.class);
        verify(linkRepository).saveAndFlush(captor.capture());
        Link persisted = captor.getValue();

        assertEquals("abc1234", created.getShortCode());
        assertEquals("https://example.com/landing", created.getOriginalUrl());
        assertEquals(Instant.parse("2026-08-17T23:30:00Z"), created.getCreatedAt());
        assertEquals(0L, created.getClickCount());
        assertEquals("abc1234", persisted.getShortCode());
    }

    @Test
    void createLinkRejectsMalformedUrl() {
        assertThrows(InvalidOriginalUrlException.class, () -> linkService.createLink("https:///missing-host"));

        verifyNoInteractions(shortCodeGenerator, linkRepository);
    }

    @Test
    void createLinkRejectsUnsupportedScheme() {
        assertThrows(InvalidOriginalUrlException.class, () -> linkService.createLink("ftp://example.com/file.txt"));

        verifyNoInteractions(shortCodeGenerator, linkRepository);
    }

    @Test
    void createLinkRetriesOnShortCodeCollision() {
        when(shortCodeGenerator.generateCandidate()).thenReturn("aaaaaaa", "bbbbbbb");
        when(linkRepository.saveAndFlush(any(Link.class)))
                .thenThrow(uniqueViolation())
                .thenAnswer(invocation -> invocation.getArgument(0));

        Link created = linkService.createLink("http://example.com");

        assertEquals("bbbbbbb", created.getShortCode());
        verify(shortCodeGenerator, times(2)).generateCandidate();
        verify(linkRepository, times(2)).saveAndFlush(any(Link.class));
    }

    @Test
    void createLinkFailsAfterRetryExhaustion() {
        when(shortCodeGenerator.generateCandidate()).thenReturn("aaaaaaa", "bbbbbbb", "ccccccc", "ddddddd", "eeeeeee");
        when(linkRepository.saveAndFlush(any(Link.class))).thenThrow(uniqueViolation());

        assertThrows(ShortCodeGenerationException.class, () -> linkService.createLink("https://example.com"));

        verify(shortCodeGenerator, times(5)).generateCandidate();
        verify(linkRepository, times(5)).saveAndFlush(any(Link.class));
    }

    @Test
    void createLinkDoesNotRetryNonUniqueConstraintViolation() {
        when(shortCodeGenerator.generateCandidate()).thenReturn("abc1234");
        when(linkRepository.saveAndFlush(any(Link.class))).thenThrow(new DataIntegrityViolationException("not a unique violation"));

        assertThrows(DataIntegrityViolationException.class, () -> linkService.createLink("https://example.com"));

        verify(shortCodeGenerator).generateCandidate();
        verify(linkRepository).saveAndFlush(any(Link.class));
    }

    private DataIntegrityViolationException uniqueViolation() {
        return new DataIntegrityViolationException("duplicate key", new SQLException("duplicate key", "23505"));
    }
}
