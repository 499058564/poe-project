package com.poe.cache.dao;

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

class TranslationDaoTest {

    private Connection connection;
    private TranslationDao dao;

    private static final String[] MIGRATION_FILES = {
        "001_base_items.sql",
        "002_skill_gems.sql",
        "003_passive_skills.sql",
        "004_mods.sql",
        "005_data_version.sql",
        "006_translations.sql",
        "007_items_fts.sql",
    };

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        runMigrations();
        dao = new TranslationDao(connection);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    @DisplayName("saveTranslation and translate single")
    void shouldSaveAndTranslate() {
        dao.saveTranslation("Iron Hat", "铁盔", "item");

        Optional<String> result = dao.translate("Iron Hat", "item");
        assertTrue(result.isPresent());
        assertEquals("铁盔", result.get());
    }

    @Test
    @DisplayName("translate returns empty for missing translation")
    void shouldReturnEmptyForMissing() {
        Optional<String> result = dao.translate("NonExistent", "item");
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("translations are domain-specific")
    void shouldRespectDomain() {
        dao.saveTranslation("Iron Hat", "铁盔", "item");

        Optional<String> result = dao.translate("Iron Hat", "skill");
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("same source + domain overwrites previous")
    void shouldOverwriteTranslation() {
        dao.saveTranslation("Arc", "电弧", "skill");
        dao.saveTranslation("Arc", "电弧技能", "skill");

        Optional<String> result = dao.translate("Arc", "skill");
        assertTrue(result.isPresent());
        assertEquals("电弧技能", result.get());
    }

    @Test
    @DisplayName("batchTranslate returns all matching")
    void shouldBatchTranslate() {
        dao.saveTranslation("Arc", "电弧", "skill");
        dao.saveTranslation("Spark", "电球", "skill");
        dao.saveTranslation("Fireball", "火球", "skill");

        List<String> sources = Arrays.asList("Arc", "Spark", "Fireball", "NonExistent");
        Map<String, String> results = dao.batchTranslate(sources, "skill");

        assertEquals(3, results.size());
        assertEquals("电弧", results.get("Arc"));
        assertEquals("电球", results.get("Spark"));
        assertEquals("火球", results.get("Fireball"));
        assertNull(results.get("NonExistent"));
    }

    @Test
    @DisplayName("batchTranslate with empty list")
    void shouldHandleEmptyBatchTranslate() {
        Map<String, String> results = dao.batchTranslate(Collections.emptyList(), "item");
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("batchSave stores multiple translations")
    void shouldBatchSave() {
        Map<String, String> translations = new LinkedHashMap<>();
        translations.put("Arc", "电弧");
        translations.put("Spark", "电球");
        translations.put("Fireball", "火球");

        dao.batchSave(translations, "skill");

        Map<String, String> results = dao.batchTranslate(
            Arrays.asList("Arc", "Spark", "Fireball"), "skill");
        assertEquals(3, results.size());
        assertEquals("电弧", results.get("Arc"));
        assertEquals("电球", results.get("Spark"));
        assertEquals("火球", results.get("Fireball"));
    }

    @Test
    @DisplayName("saveTranslation across multiple domains")
    void shouldSupportMultipleDomains() {
        dao.saveTranslation("Iron Hat", "铁盔", "item");
        dao.saveTranslation("Iron Hat", "铁帽", "skill");

        assertEquals("铁盔", dao.translate("Iron Hat", "item").get());
        assertEquals("铁帽", dao.translate("Iron Hat", "skill").get());
    }

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
