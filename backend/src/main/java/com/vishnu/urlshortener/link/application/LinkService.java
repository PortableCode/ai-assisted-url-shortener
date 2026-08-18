package com.vishnu.urlshortener.link.application;

import com.vishnu.urlshortener.link.domain.Link;
import com.vishnu.urlshortener.link.infrastructure.ShortCodeGenerator;
import com.vishnu.urlshortener.link.persistence.LinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

@Service
public class LinkService {

    private static final int MAX_RETRY_ATTEMPTS = 5;
    private static final String UNIQUE_VIOLATION_SQL_STATE = "23505";

    private final LinkRepository linkRepository;
    private final ShortCodeGenerator shortCodeGenerator;
    private final Clock clock;

    @Autowired
    public LinkService(LinkRepository linkRepository, ShortCodeGenerator shortCodeGenerator) {
        this(linkRepository, shortCodeGenerator, Clock.systemUTC());
    }

    LinkService(LinkRepository linkRepository, ShortCodeGenerator shortCodeGenerator, Clock clock) {
        this.linkRepository = Objects.requireNonNull(linkRepository, "linkRepository must not be null");
        this.shortCodeGenerator = Objects.requireNonNull(shortCodeGenerator, "shortCodeGenerator must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    public Link createLink(String originalUrl) {
        String validatedUrl = validateOriginalUrl(originalUrl);

        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            Link link = new Link(shortCodeGenerator.generateCandidate(), validatedUrl, Instant.now(clock));
            try {
                return linkRepository.saveAndFlush(link);
            }
            catch (DataIntegrityViolationException ex) {
                if (!isUniqueConstraintViolation(ex)) {
                    throw ex;
                }
                if (attempt == MAX_RETRY_ATTEMPTS) {
                    throw new ShortCodeGenerationException("Unable to generate a unique short code after 5 attempts", ex);
                }
            }
        }

        throw new ShortCodeGenerationException("Unable to generate a unique short code after 5 attempts", null);
    }

    private String validateOriginalUrl(String originalUrl) {
        if (originalUrl == null || originalUrl.isBlank()) {
            throw new InvalidOriginalUrlException("originalUrl must not be blank");
        }

        URI uri;
        try {
            uri = new URI(originalUrl).parseServerAuthority();
        }
        catch (URISyntaxException | IllegalArgumentException ex) {
            throw new InvalidOriginalUrlException("originalUrl must be a valid HTTP or HTTPS URL", ex);
        }

        String scheme = uri.getScheme();
        if (scheme == null) {
            throw new InvalidOriginalUrlException("originalUrl must use http or https");
        }

        String normalizedScheme = scheme.toLowerCase(Locale.ROOT);
        if (!normalizedScheme.equals("http") && !normalizedScheme.equals("https")) {
            throw new InvalidOriginalUrlException("originalUrl must use http or https");
        }

        if (!uri.isAbsolute() || uri.getHost() == null || uri.getHost().isBlank()) {
            throw new InvalidOriginalUrlException("originalUrl must include a valid host");
        }

        return originalUrl;
    }

    private boolean isUniqueConstraintViolation(DataIntegrityViolationException ex) {
        Throwable current = ex;
        while (current != null) {
            if (current instanceof SQLException sqlException && UNIQUE_VIOLATION_SQL_STATE.equals(sqlException.getSQLState())) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
