package com.dt.digitaltwinsimulator.logic;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates random values for simulator format definitions.
 *
 * <p>Supported randomCondition syntax:</p>
 * <ul>
 *     <li>String: {@code 10} = fixed length 10, {@code 5..20} = random length between 5 and 20</li>
 *     <li>Integer: {@code 100} = 0..100, {@code 10..20} = 10..20</li>
 *     <li>Double: {@code 100.5} = 0..100.5, {@code 10.5..20.5} = 10.5..20.5</li>
 *     <li>Boolean: condition is ignored</li>
 *     <li>Date: empty = yyyyMMddHHmmssSSS, otherwise condition is used as DateTimeFormatter pattern</li>
 * </ul>
 */
public final class RandomValueGenerator {
    private static final String ALPHA_NUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final DateTimeFormatter DEFAULT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private RandomValueGenerator() {
    }

    public static String generate(String dataType, String randomCondition) {
        String normalizedType = dataType == null ? "" : dataType.trim().toLowerCase(Locale.ROOT);
        String condition = randomCondition == null ? "" : randomCondition.trim();

        return switch (normalizedType) {
            case "string" -> randomString(condition);
            case "integer", "int", "long" -> String.valueOf(randomInteger(condition));
            case "double", "float", "number" -> String.valueOf(randomDouble(condition));
            case "boolean", "bool" -> String.valueOf(ThreadLocalRandom.current().nextBoolean());
            case "date", "datetime", "timestamp" -> randomDate(condition);
            default -> "";
        };
    }

    public static boolean isRandomEnabled(String value) {
        if (value == null) {
            return false;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return "1".equals(normalized) || "true".equals(normalized) || "y".equals(normalized) || "yes".equals(normalized);
    }

    private static String randomString(String condition) {
        IntRange range = parseIntRange(condition, 10, 10, true);
        int length = randomIntBetween(range.min(), range.max());
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = ThreadLocalRandom.current().nextInt(ALPHA_NUMERIC.length());
            sb.append(ALPHA_NUMERIC.charAt(index));
        }
        return sb.toString();
    }

    private static int randomInteger(String condition) {
        IntRange range = parseIntRange(condition, 0, 100, false);
        return randomIntBetween(range.min(), range.max());
    }

    private static double randomDouble(String condition) {
        DoubleRange range = parseDoubleRange(condition, 0.0, 100.0);
        double value = ThreadLocalRandom.current().nextDouble(range.min(), Math.nextUp(range.max()));
        return Math.round(value * 1000.0) / 1000.0;
    }

    private static String randomDate(String condition) {
        DateTimeFormatter formatter = condition.isBlank()
                ? DEFAULT_DATE_FORMATTER
                : DateTimeFormatter.ofPattern(condition);
        return LocalDateTime.now().format(formatter);
    }

    private static IntRange parseIntRange(String condition, int defaultMin, int defaultMax, boolean singleValueMeansExact) {
        if (condition == null || condition.isBlank()) {
            return new IntRange(defaultMin, defaultMax);
        }

        String[] parts = splitRange(condition);
        if (parts.length == 1) {
            int value = Integer.parseInt(parts[0].trim());
            return singleValueMeansExact ? new IntRange(value, value) : new IntRange(0, value);
        }

        int min = Integer.parseInt(parts[0].trim());
        int max = Integer.parseInt(parts[1].trim());
        return normalize(new IntRange(min, max));
    }

    private static DoubleRange parseDoubleRange(String condition, double defaultMin, double defaultMax) {
        if (condition == null || condition.isBlank()) {
            return new DoubleRange(defaultMin, defaultMax);
        }

        String[] parts = splitRange(condition);
        if (parts.length == 1) {
            double value = Double.parseDouble(parts[0].trim());
            return new DoubleRange(0.0, value);
        }

        double min = Double.parseDouble(parts[0].trim());
        double max = Double.parseDouble(parts[1].trim());
        return normalize(new DoubleRange(min, max));
    }

    private static String[] splitRange(String condition) {
        if (condition.contains("..")) {
            return condition.split("\\.\\.", 2);
        }
        if (condition.contains("~")) {
            return condition.split("~", 2);
        }
        if (condition.contains(":")) {
            return condition.split(":", 2);
        }
        return new String[]{condition};
    }

    private static int randomIntBetween(int min, int max) {
        if (min == max) {
            return min;
        }
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    private static IntRange normalize(IntRange range) {
        return range.min() <= range.max() ? range : new IntRange(range.max(), range.min());
    }

    private static DoubleRange normalize(DoubleRange range) {
        return range.min() <= range.max() ? range : new DoubleRange(range.max(), range.min());
    }

    private record IntRange(int min, int max) {
    }

    private record DoubleRange(double min, double max) {
    }
}
