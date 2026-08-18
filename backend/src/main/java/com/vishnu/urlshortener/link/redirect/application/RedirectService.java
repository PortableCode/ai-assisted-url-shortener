package com.vishnu.urlshortener.link.redirect.application;

import com.vishnu.urlshortener.link.application.LinkNotFoundException;
import com.vishnu.urlshortener.link.application.LinkExpiredException;
import com.vishnu.urlshortener.link.domain.Link;
import com.vishnu.urlshortener.link.persistence.LinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
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
        Instant accessedAt = Instant.now(clock);

        Link link = linkRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new LinkNotFoundException(shortCode));

        if (isExpired(link, accessedAt)) {
            throw new LinkExpiredException(shortCode);
        }

        int updatedRows = linkRepository.incrementAccessStats(shortCode, accessedAt);
        if (updatedRows == 0) {
            if (linkRepository.findByShortCode(shortCode).isEmpty()) {
                throw new LinkNotFoundException(shortCode);
            }
            if (isExpired(link, accessedAt)) {
                throw new LinkExpiredException(shortCode);
            }
            throw new LinkNotFoundException(shortCode);
        }

        return link.getOriginalUrl();
    }

    private boolean isExpired(Link link, Instant accessedAt) {
        Instant expiresAt = link.getExpiresAt();
        return expiresAt != null && !expiresAt.isAfter(accessedAt);
    }

    private void validateShortCode(String shortCode) {
        if (shortCode == null || !SHORT_CODE_PATTERN.matcher(shortCode).matches()) {
            throw new LinkNotFoundException(String.valueOf(shortCode));
        }
    }
}
