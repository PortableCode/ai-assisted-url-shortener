package com.vishnu.urlshortener.link.infrastructure;

import java.security.SecureRandom;
import java.util.Objects;

public final class ShortCodeGenerator {

    private static final int SHORT_CODE_LENGTH = 7;
    private static final char[] BASE62 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();

    private final SecureRandom secureRandom;

    public ShortCodeGenerator() {
        this(new SecureRandom());
    }

    ShortCodeGenerator(SecureRandom secureRandom) {
        this.secureRandom = Objects.requireNonNull(secureRandom, "secureRandom must not be null");
    }

    public String generateCandidate() {
        char[] candidate = new char[SHORT_CODE_LENGTH];
        for (int i = 0; i < candidate.length; i++) {
            candidate[i] = BASE62[secureRandom.nextInt(BASE62.length)];
        }
        return new String(candidate);
    }
}
