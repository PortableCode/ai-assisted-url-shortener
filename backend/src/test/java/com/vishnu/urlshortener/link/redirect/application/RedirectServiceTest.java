package com.vishnu.urlshortener.link.redirect.application;

import com.vishnu.urlshortener.link.domain.Link;
import com.vishnu.urlshortener.link.persistence.LinkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedirectServiceTest {

    @Mock
    private LinkRepository linkRepository;

    private RedirectService redirectService;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-08-18T00:00:00Z"), ZoneOffset.UTC);
        redirectService = new RedirectService(linkRepository, fixedClock);
    }

    @Test
    void redirectReturnsOriginalUrlForExistingCode() {
        Link link = new Link("abc1234", "https://example.com/landing", Instant.parse("2026-08-17T23:59:00Z"));
        when(linkRepository.findByShortCode("abc1234")).thenReturn(Optional.of(link));
        when(linkRepository.incrementAccessStats(eq("abc1234"), any(Instant.class))).thenReturn(1);

        String originalUrl = redirectService.redirect("abc1234");

        assertEquals("https://example.com/landing", originalUrl);
        verify(linkRepository).findByShortCode("abc1234");
        verify(linkRepository).incrementAccessStats(eq("abc1234"), eq(Instant.parse("2026-08-18T00:00:00Z")));
    }

    @Test
    void redirectThrowsNotFoundForUnknownCode() {
        when(linkRepository.findByShortCode("abc1234")).thenReturn(Optional.empty());

        assertThrows(ShortCodeNotFoundException.class, () -> redirectService.redirect("abc1234"));

        verify(linkRepository).findByShortCode("abc1234");
        verify(linkRepository, never()).incrementAccessStats(any(), any());
    }

    @Test
    void redirectThrowsNotFoundWhenAtomicUpdateTouchesZeroRows() {
        Link link = new Link("abc1234", "https://example.com/landing", Instant.parse("2026-08-17T23:59:00Z"));
        when(linkRepository.findByShortCode("abc1234")).thenReturn(Optional.of(link));
        when(linkRepository.incrementAccessStats(eq("abc1234"), any(Instant.class))).thenReturn(0);

        assertThrows(ShortCodeNotFoundException.class, () -> redirectService.redirect("abc1234"));

        verify(linkRepository).findByShortCode("abc1234");
        verify(linkRepository).incrementAccessStats(eq("abc1234"), eq(Instant.parse("2026-08-18T00:00:00Z")));
    }
}
