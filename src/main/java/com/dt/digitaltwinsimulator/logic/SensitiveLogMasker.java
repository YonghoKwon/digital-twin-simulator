package com.dt.digitaltwinsimulator.logic;

import java.util.regex.Pattern;

public final class SensitiveLogMasker {
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("(?i)(\"?(pw|password)\"?\\s*[:=]\\s*\"?)([^\",}\\s]+)");
    private static final Pattern USER_PATTERN = Pattern.compile("(?i)(\"?(id|username|user)\"?\\s*[:=]\\s*\"?)([^\",}\\s]+)");

    private SensitiveLogMasker() {
    }

    public static String mask(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        String masked = PASSWORD_PATTERN.matcher(value).replaceAll("$1****");
        return USER_PATTERN.matcher(masked).replaceAll("$1****");
    }
}
