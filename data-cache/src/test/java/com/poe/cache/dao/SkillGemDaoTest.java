package com.poe.cache.dao;

import com.poe.cache.model.SkillGem;
import org.junit.jupiter.api.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class SkillGemDaoTest {

    private Connection connection;
    private SkillGemDao dao;

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
        dao = new SkillGemDao(connection);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    @DisplayName("insert and findById")
    void shouldInsertAndFindById() {
        SkillGem gem = new SkillGem(1, "Arc", "active", "3.24");
        gem.setGemTags("[\"spell\", \"lightning\", \"chaining\"]");
        gem.setPrimaryAttribute("int");
        dao.insert(gem);

        Optional<SkillGem> found = dao.findById(1);
        assertTrue(found.isPresent());
        assertEquals("Arc", found.get().getName());
        assertEquals("active", found.get().getGemType());
        assertEquals("int", found.get().getPrimaryAttribute());
    }

    @Test
    @DisplayName("insert support gem")
    void shouldInsertSupportGem() {
        SkillGem gem = new SkillGem(2, "Added Lightning Damage", "support", "3.24");
        gem.setDescription("Adds lightning damage to attacks");
        dao.insert(gem);

        Optional<SkillGem> found = dao.findById(2);
        assertTrue(found.isPresent());
        assertEquals("support", found.get().getGemType());
    }

    @Test
    @DisplayName("findAll returns all gems")
    void shouldFindAll() {
        dao.insert(new SkillGem(1, "Arc", "active", "3.24"));
        dao.insert(new SkillGem(2, "Spark", "active", "3.24"));
        dao.insert(new SkillGem(3, "Added Cold Damage", "support", "3.24"));

        assertEquals(3, dao.findAll().size());
    }

    @Test
    @DisplayName("count returns correct count")
    void shouldCount() {
        assertEquals(0, dao.count());
        dao.insert(new SkillGem(1, "Arc", "active", "3.24"));
        assertEquals(1, dao.count());
    }

    @Test
    @DisplayName("deleteById removes gem")
    void shouldDeleteById() {
        dao.insert(new SkillGem(1, "Arc", "active", "3.24"));
        dao.deleteById(1);
        assertEquals(0, dao.count());
    }

    @Test
    @DisplayName("findById returns empty for missing")
    void shouldReturnEmptyForMissing() {
        assertFalse(dao.findById(99999).isPresent());
    }

    @Test
    @DisplayName("batch insert 1000 gems under 1 second")
    void shouldBatchInsertWithinOneSecond() {
        List<SkillGem> gems = new ArrayList<>();
        for (int i = 1; i <= 1000; i++) {
            gems.add(new SkillGem(i, "Gem_" + i, "active", "3.24"));
        }

        long start = System.currentTimeMillis();
        dao.batchInsert(gems);
        long elapsed = System.currentTimeMillis() - start;

        assertEquals(1000, dao.count());
        assertTrue(elapsed < 1000, "Batch insert took " + elapsed + "ms");
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
