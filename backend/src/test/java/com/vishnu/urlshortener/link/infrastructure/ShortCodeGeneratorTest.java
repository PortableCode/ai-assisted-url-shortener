package com.vishnu.urlshortener.link.infrastructure;

import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShortCodeGeneratorTest {

    private static final Pattern BASE62_PATTERN = Pattern.compile("^[a-zA-Z0-9]{7}$");

    @Test
    void generateCandidateProducesSevenCharacterBase62Codes() {
        ShortCodeGenerator generator = new ShortCodeGenerator();

        for (int i = 0; i < 100; i++) {
            String candidate = generator.generateCandidate();

            assertEquals(7, candidate.length());
            assertTrue(BASE62_PATTERN.matcher(candidate).matches(), () -> "Invalid candidate: " + candidate);
        }
    }
}
