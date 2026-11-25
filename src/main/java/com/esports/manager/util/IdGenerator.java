package com.esports.manager.util;

import java.util.Locale;
import java.util.UUID;

/** Generates IDs in the application's PREFIX-XXXXXX format. */
public final class IdGenerator {

    private IdGenerator() {
    }

    public static String next(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            throw new IllegalArgumentException("ID prefix must not be blank");
        }
        String normalizedPrefix = prefix.trim().toUpperCase(Locale.ROOT);
        String suffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase(Locale.ROOT);
        return normalizedPrefix + "-" + suffix;
    }
}
