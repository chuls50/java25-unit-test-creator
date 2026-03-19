package com.example.util;

/**
 * Utility methods for common String operations.
 */
public class StringUtils {

    private StringUtils() {}

    /**
     * Returns true if the string is null, empty, or contains only whitespace.
     */
    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Capitalizes the first letter and lowercases the rest.
     * Returns the input unchanged if null or empty.
     */
    public static String capitalize(String value) {
        if (value == null || value.isEmpty()) return value;
        return Character.toUpperCase(value.charAt(0)) + value.substring(1).toLowerCase();
    }

    /**
     * Truncates a string to maxLength characters, appending "..." if truncated.
     * Throws IllegalArgumentException if maxLength is less than 1.
     */
    public static String truncate(String value, int maxLength) {
        if (maxLength < 1) throw new IllegalArgumentException("maxLength must be >= 1");
        if (value == null) return null;
        if (value.length() <= maxLength) return value;
        return value.substring(0, maxLength) + "...";
    }

    /**
     * Reverses a string. Returns null if input is null.
     */
    public static String reverse(String value) {
        if (value == null) return null;
        return new StringBuilder(value).reverse().toString();
    }
}
