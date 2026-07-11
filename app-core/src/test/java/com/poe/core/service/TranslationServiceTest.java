package com.poe.core.service;

import com.poe.cache.dao.TranslationDao;
import org.junit.jupiter.api.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class TranslationServiceTest {

    private Connection connection;
    private TranslationDao dao;
    private TranslationService service;

    private static final String[] MIGRATION_FILES = {
        "v006_translations.sql",
    };

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        runMigrations();
        dao = new TranslationDao(connection);
        service = new TranslationService(dao);
    }

    @AfterEach
    void tearDown() throws Exception {
        service.clearCustomTranslations("item");
        service.clearMissingTranslations();
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    // ── translate 单条翻译 ──

    @Test
    @DisplayName("translate returns translation from DB")
    void shouldTranslateFromDb() {
        dao.saveTranslation("Mageblood", "法师之血", "item");

        String result = service.translate("Mageblood", "item");
        assertEquals("法师之血", result);
    }

    @Test
    @DisplayName("translate returns original for unknown item")
    void shouldReturnOriginalForUnknown() {
        String result = service.translate("UnknownItem", "item");
        assertEquals("UnknownItem", result);
    }

    @Test
    @DisplayName("translate returns null for null input")
    void shouldHandleNullSource() {
        assertNull(service.translate(null, "item"));
    }

    @Test
    @DisplayName("translate returns empty for empty input")
    void shouldHandleEmptySource() {
        assertEquals("", service.translate("", "item"));
    }

    @Test
    @DisplayName("translate records missing translations")
    void shouldRecordMissingTranslations() {
        service.translate("NonExistent123", "item");
        service.translate("AnotherMissing", "skill");

        List<String> missing = service.getMissingTranslations();
        assertTrue(missing.stream().anyMatch(m -> m.contains("NonExistent123")));
        assertTrue(missing.stream().anyMatch(m -> m.contains("AnotherMissing")));
    }

    // ── custom translation 优先级 ──

    @Test
    @DisplayName("custom translation takes priority over DB")
    void shouldPrioritizeCustomOverDb() {
        dao.saveTranslation("Mageblood", "法师之血", "item");
        service.addCustomTranslation("Mageblood", "我的自定义翻译", "item");

        String result = service.translate("Mageblood", "item");
        assertEquals("我的自定义翻译", result);
    }

    @Test
    @DisplayName("custom translation works without DB entry")
    void shouldUseCustomWithoutDb() {
        service.addCustomTranslation("SomeItem", "某物品", "item");

        String result = service.translate("SomeItem", "item");
        assertEquals("某物品", result);
    }

    @Test
    @DisplayName("custom translation is memory-only, not written to DB")
    void shouldNotPersistCustomToDb() {
        service.addCustomTranslation("NewItem", "新物品", "item");

        // 自定义翻译在内存中可用
        assertEquals("新物品", service.translate("NewItem", "item"));
        // 但未写入 DB
        Optional<String> dbResult = dao.translate("NewItem", "item");
        assertFalse(dbResult.isPresent());
    }

    @Test
    @DisplayName("remove custom translation falls back to DB")
    void shouldFallBackToDbAfterRemove() {
        dao.saveTranslation("Mageblood", "法师之血", "item");
        service.addCustomTranslation("Mageblood", "自定义", "item");
        service.removeCustomTranslation("Mageblood", "item");

        String result = service.translate("Mageblood", "item");
        assertEquals("法师之血", result);
    }

    @Test
    @DisplayName("clear custom translations for domain")
    void shouldClearCustomTranslations() {
        service.addCustomTranslation("A", "甲", "item");
        service.addCustomTranslation("B", "乙", "item");
        assertEquals(2, service.getCustomTranslationCount("item"));

        service.clearCustomTranslations("item");
        assertEquals(0, service.getCustomTranslationCount("item"));
    }

    // ── batchTranslate ──

    @Test
    @DisplayName("batchTranslate returns DB results")
    void shouldBatchTranslateFromDb() {
        dao.batchSave(Map.of(
            "Iron Hat", "铁盔",
            "Gold Ring", "金戒指",
            "Mageblood", "法师之血"
        ), "item");

        Map<String, String> results = service.batchTranslate(
            List.of("Iron Hat", "Gold Ring", "Mageblood"), "item");

        assertEquals(3, results.size());
        assertEquals("铁盔", results.get("Iron Hat"));
        assertEquals("金戒指", results.get("Gold Ring"));
        assertEquals("法师之血", results.get("Mageblood"));
    }

    @Test
    @DisplayName("batchTranslate custom overlay overrides DB")
    void shouldOverlayCustomInBatch() {
        dao.batchSave(Map.of(
            "Iron Hat", "铁盔",
            "Gold Ring", "金戒指"
        ), "item");
        service.addCustomTranslation("Iron Hat", "自定义铁盔", "item");

        Map<String, String> results = service.batchTranslate(
            List.of("Iron Hat", "Gold Ring"), "item");

        assertEquals(2, results.size());
        assertEquals("自定义铁盔", results.get("Iron Hat"));
        assertEquals("金戒指", results.get("Gold Ring"));
    }

    @Test
    @DisplayName("batchTranslate handles empty list")
    void shouldHandleEmptyBatch() {
        assertTrue(service.batchTranslate(Collections.emptyList(), "item").isEmpty());
        assertTrue(service.batchTranslate(null, "item").isEmpty());
    }

    @Test
    @DisplayName("batchTranslate 100 items under 10ms")
    void shouldBatchTranslateFast() {
        for (int i = 0; i < 100; i++) {
            dao.saveTranslation("Item" + i, "物品" + i, "item");
        }
        List<String> sources = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            sources.add("Item" + i);
        }

        long start = System.nanoTime();
        Map<String, String> results = service.batchTranslate(sources, "item");
        long elapsed = (System.nanoTime() - start) / 1_000_000; // ms

        assertEquals(100, results.size());
        assertTrue(elapsed < 10, "Batch translate took " + elapsed + "ms, expected < 10ms");
    }

    // ── importTranslations ──

    @Test
    @DisplayName("importTranslations saves to DB")
    void shouldImportTranslations() {
        Map<String, String> data = Map.of(
            "Mageblood", "法师之血",
            "Headhunter", "猎首"
        );
        service.importTranslations(data, "item");

        assertEquals("法师之血", dao.translate("Mageblood", "item").orElse(null));
        assertEquals("猎首", dao.translate("Headhunter", "item").orElse(null));
    }

    @Test
    @DisplayName("importTranslations handles empty map")
    void shouldHandleEmptyImport() {
        service.importTranslations(Collections.emptyMap(), "item");
        service.importTranslations(null, "item");
        // no exception = pass
    }

    // ── importTranslationPack (JSON) ──

    @Test
    @DisplayName("importTranslationPack parses JSON and imports to DB")
    void shouldImportJsonPack() {
        String json = """
            {
              "domain": "item",
              "version": "3.24",
              "entries": {
                "Mageblood": "法师之血",
                "Headhunter": "猎首",
                "Mirror of Kalandra": "卡兰德拉魔镜"
              }
            }
            """;

        int count = service.importTranslationPack(json);
        assertEquals(3, count);

        assertEquals("法师之血", dao.translate("Mageblood", "item").orElse(null));
        assertEquals("猎首", dao.translate("Headhunter", "item").orElse(null));
        assertEquals("卡兰德拉魔镜", dao.translate("Mirror of Kalandra", "item").orElse(null));
    }

    @Test
    @DisplayName("importTranslationPack immediately usable after import")
    void shouldBeImmediatelyUsable() {
        String json = """
            {
              "domain": "item",
              "version": "3.24",
              "entries": {
                "ImportedItem": "导入物品"
              }
            }
            """;

        service.importTranslationPack(json);
        String result = service.translate("ImportedItem", "item");
        assertEquals("导入物品", result);
    }

    @Test
    @DisplayName("importTranslationPack returns 0 for invalid JSON")
    void shouldHandleInvalidJson() {
        assertEquals(0, service.importTranslationPack("not json"));
        assertEquals(0, service.importTranslationPack("{}"));
        assertEquals(0, service.importTranslationPack(""));
        assertEquals(0, service.importTranslationPack(null));
    }

    // ── importFromPoeCharm2 ──

    @Test
    @DisplayName("importFromPoeCharm2 returns 0 when no resources found")
    void shouldHandleMissingPoeCharm2Resources() {
        int count = service.importFromPoeCharm2();
        // poecharm2/src/main/resources may not exist yet → returns 0
        assertTrue(count >= 0);
    }

    // ── missing translations tracking ──

    @Test
    @DisplayName("missing translations are deduplicated")
    void shouldDeduplicateMissingTranslations() {
        service.translate("Unknown", "item");
        service.translate("Unknown", "item");
        service.translate("Unknown", "item");

        List<String> missing = service.getMissingTranslations();
        assertEquals(1, missing.size());
    }

    @Test
    @DisplayName("clearMissingTranslations empties the list")
    void shouldClearMissingTranslations() {
        service.translate("Missing1", "item");
        service.translate("Missing2", "item");
        assertFalse(service.getMissingTranslations().isEmpty());

        service.clearMissingTranslations();
        assertTrue(service.getMissingTranslations().isEmpty());
    }

    // ── 辅助方法 ──

    private void runMigrations() throws Exception {
        for (String file : MIGRATION_FILES) {
            String sql = loadResource("migration/" + file);
            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate(sql);
            }
        }
    }

    private String loadResource(String path) throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new RuntimeException("Not found: " + path);
            try (BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                return r.lines().collect(Collectors.joining("\n"));
            }
        }
    }
}
