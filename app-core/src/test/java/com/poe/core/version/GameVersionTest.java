package com.poe.core.version;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GameVersion 单元测试。
 */
class GameVersionTest {

    @Test
    @DisplayName("构造应正确存储所有字段")
    void shouldStoreAllFields() {
        Instant now = Instant.now();
        GameVersion version = new GameVersion("3.25", "Settlers", "GGG API", now);

        assertEquals("3.25", version.getVersion());
        assertEquals("Settlers", version.getLeague());
        assertEquals("GGG API", version.getSource());
        assertEquals(now, version.getDetectedAt());
    }

    @Test
    @DisplayName("detectedAt 为 null 时使用 Instant.now()")
    void shouldDefaultToNowWhenDetectedAtIsNull() {
        GameVersion version = new GameVersion("3.25", "Settlers", "GGG API", null);
        assertNotNull(version.getDetectedAt());
    }

    @Test
    @DisplayName("unknown() 哨兵版本应返回 isUnknown() = true")
    void shouldReturnUnknownSentinel() {
        GameVersion version = GameVersion.unknown();
        assertTrue(version.isUnknown());
        assertEquals("unknown", version.getVersion());
        assertEquals("unknown", version.getLeague());
        assertEquals("none", version.getSource());
    }

    @Test
    @DisplayName("已知版本 isUnknown() 应返回 false")
    void shouldNotBeUnknownForKnownVersion() {
        GameVersion version = new GameVersion("3.25", "Settlers", "GGG API", Instant.now());
        assertFalse(version.isUnknown());
    }

    @Nested
    @DisplayName("equals 和 hashCode")
    class EqualsAndHashCode {

        @Test
        @DisplayName("相同字段应相等")
        void shouldBeEqualWhenFieldsMatch() {
            Instant now = Instant.now();
            GameVersion v1 = new GameVersion("3.25", "Settlers", "GGG API", now);
            GameVersion v2 = new GameVersion("3.25", "Settlers", "GGG API", Instant.now());
            assertEquals(v1, v2);
            assertEquals(v1.hashCode(), v2.hashCode());
        }

        @Test
        @DisplayName("不同版本不应相等")
        void shouldNotBeEqualWithDifferentVersion() {
            GameVersion v1 = new GameVersion("3.25", "Settlers", "GGG API", Instant.now());
            GameVersion v2 = new GameVersion("3.24", "Settlers", "GGG API", Instant.now());
            assertNotEquals(v1, v2);
        }

        @Test
        @DisplayName("不同来源不应相等")
        void shouldNotBeEqualWithDifferentSource() {
            GameVersion v1 = new GameVersion("3.25", "Settlers", "GGG API", Instant.now());
            GameVersion v2 = new GameVersion("3.25", "Settlers", "Wiki", Instant.now());
            assertNotEquals(v1, v2);
        }
    }
}
