package com.example.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StringUtils.
 *
 * Class type: Static utility — no setup needed, methods called directly.
 * Prompt used: "Write unit tests for this class"
 */
class StringUtilsTest {

    // -------------------------------------------------------------------------
    // isBlank
    // -------------------------------------------------------------------------

    @ParameterizedTest(name = "isBlank(\"{0}\") should return true")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n", "\r\n"})
    void isBlank_whenNullOrWhitespace_thenReturnsTrue(String input) {
        assertTrue(StringUtils.isBlank(input));
    }

    @ParameterizedTest(name = "isBlank(\"{0}\") should return false")
    @ValueSource(strings = {"a", "hello", "  hi  ", "123"})
    void isBlank_whenNonBlankContent_thenReturnsFalse(String input) {
        assertFalse(StringUtils.isBlank(input));
    }

    // -------------------------------------------------------------------------
    // capitalize
    // -------------------------------------------------------------------------

    @ParameterizedTest(name = "capitalize(\"{0}\") = \"{1}\"")
    @CsvSource({
        "hello,   Hello",
        "WORLD,   World",
        "jAvA,    Java",
        "a,       A",
        "already, Already"
    })
    void capitalize_givenVariousInputs_thenNormalizesCorrectly(String input, String expected) {
        assertEquals(expected.trim(), StringUtils.capitalize(input));
    }

    @Test
    void capitalize_whenNull_thenReturnsNull() {
        assertNull(StringUtils.capitalize(null));
    }

    @Test
    void capitalize_whenEmpty_thenReturnsEmpty() {
        assertEquals("", StringUtils.capitalize(""));
    }

    // -------------------------------------------------------------------------
    // truncate
    // -------------------------------------------------------------------------

    @Test
    void truncate_whenValueShorterThanMax_thenReturnsUnchanged() {
        assertEquals("hello", StringUtils.truncate("hello", 10));
    }

    @Test
    void truncate_whenValueExceedsMax_thenTruncatesAndAppendsEllipsis() {
        assertEquals("hel...", StringUtils.truncate("hello world", 3));
    }

    @Test
    void truncate_whenValueExactlyMaxLength_thenReturnsUnchanged() {
        assertEquals("hello", StringUtils.truncate("hello", 5));
    }

    @Test
    void truncate_whenNull_thenReturnsNull() {
        assertNull(StringUtils.truncate(null, 5));
    }

    @Test
    void truncate_whenMaxLengthIsZero_thenThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> StringUtils.truncate("hello", 0));
    }

    @Test
    void truncate_whenMaxLengthIsNegative_thenThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> StringUtils.truncate("hello", -5));
    }

    // -------------------------------------------------------------------------
    // reverse
    // -------------------------------------------------------------------------

    @ParameterizedTest(name = "reverse(\"{0}\") = \"{1}\"")
    @CsvSource({
        "hello,   olleh",
        "abcde,   edcba",
        "a,       a",
        "'',      ''"
    })
    void reverse_givenString_thenReturnsReversed(String input, String expected) {
        assertEquals(expected, StringUtils.reverse(input));
    }

    @Test
    void reverse_whenNull_thenReturnsNull() {
        assertNull(StringUtils.reverse(null));
    }
}
