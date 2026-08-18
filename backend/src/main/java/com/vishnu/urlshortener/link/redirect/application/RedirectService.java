package com.vishnu.urlshortener.link.redirect.application;

import com.vishnu.urlshortener.link.domain.Link;
import com.vishnu.urlshortener.link.persistence.LinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class RedirectService {

    private static final Pattern SHORT_CODE_PATTERN = Pattern.compile("^[a-zA-Z0-9]{7}$");

    private final LinkRepository linkRepository;
    private final Clock clock;

    @Autowired
    public RedirectService(LinkRepository linkRepository) {
        this(linkRepository, Clock.systemUTC());
    }

    RedirectService(LinkRepository linkRepository, Clock clock) {
        this.linkRepository = Objects.requireNonNull(linkRepository, "linkRepository must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Transactional
    public String redirect(String shortCode) {
        validateShortCode(shortCode);

        Link link = linkRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortCodeNotFoundException(shortCode));

        Instant accessedAt = Instant.now(clock);
        int updatedRows = linkRepository.incrementAccessStats(shortCode, accessedAt);
        if (updatedRows == 0) {
            throw new ShortCodeNotFoundException(shortCode);
        }

        return link.getOriginalUrl();
    }

    private void validateShortCode(String shortCode) {
        if (shortCode == null || !SHORT_CODE_PATTERN.matcher(shortCode).matches()) {
            throw new ShortCodeNotFoundException(String.valueOf(shortCode));
        }
    }
}
