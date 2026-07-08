package com.poe.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilsTest {

    @Test
    void isEmptyShouldReturnTrueForNull() {
        assertTrue(StringUtils.isEmpty(null));
    }

    @Test
    void isEmptyShouldReturnTrueForEmptyString() {
        assertTrue(StringUtils.isEmpty(""));
    }

    @Test
    void isEmptyShouldReturnFalseForNonEmptyString() {
        assertFalse(StringUtils.isEmpty("hello"));
        assertFalse(StringUtils.isEmpty(" "));
    }

    @Test
    void isBlankShouldReturnTrueForNull() {
        assertTrue(StringUtils.isBlank(null));
    }

    @Test
    void isBlankShouldReturnTrueForEmptyString() {
        assertTrue(StringUtils.isBlank(""));
    }

    @Test
    void isBlankShouldReturnTrueForWhitespaceOnly() {
        assertTrue(StringUtils.isBlank("   "));
        assertTrue(StringUtils.isBlank("\t\n  "));
    }

    @Test
    void isBlankShouldReturnFalseForNonBlankString() {
        assertFalse(StringUtils.isBlank("hello"));
        assertFalse(StringUtils.isBlank("  hello  "));
    }

    @Test
    void normalizeNameShouldReturnEmptyForNull() {
        assertEquals("", StringUtils.normalizeName(null));
    }

    @Test
    void normalizeNameShouldReturnEmptyForEmptyString() {
        assertEquals("", StringUtils.normalizeName(""));
    }

    @Test
    void normalizeNameShouldReturnEmptyForWhitespace() {
        assertEquals("", StringUtils.normalizeName("   "));
    }

    @Test
    void normalizeNameShouldTrimAndLowercase() {
        assertEquals("mageblood", StringUtils.normalizeName("Mage Blood"));
        assertEquals("mageblood", StringUtils.normalizeName(" Mage  Blood "));
    }

    @Test
    void normalizeNameShouldRemoveAllWhitespace() {
        assertEquals("tabularasa", StringUtils.normalizeName("Tabula Rasa"));
        assertEquals("headoftheward", StringUtils.normalizeName("Head of the Ward"));
    }

    @Test
    void normalizeNameShouldHandleSingleWord() {
        assertEquals("chaosorb", StringUtils.normalizeName("Chaos Orb"));
    }
}
