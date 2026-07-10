package com.poe.cache.dao;

import com.poe.cache.model.Mod;
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

class ModDaoTest {

    private Connection connection;
    private ModDao dao;

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
        dao = new ModDao(connection);
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
        Mod mod = new Mod(1, "Flaming", "3.24");
        mod.setModType("prefix");
        mod.setDomain("item");
        mod.setGenerationType("prefix");
        mod.setRequiredLevel(10);
        dao.insert(mod);

        Optional<Mod> found = dao.findById(1);
        assertTrue(found.isPresent());
        assertEquals("Flaming", found.get().getName());
        assertEquals("prefix", found.get().getModType());
        assertEquals(10, found.get().getRequiredLevel());
    }

    @Test
    @DisplayName("insert implicit mod")
    void shouldInsertImplicit() {
        Mod mod = new Mod(2, "of the Crusade", "3.24");
        mod.setModType("implicit");
        mod.setStats("[{\"id\":\"base_maximum_life\",\"min\":20,\"max\":30}]");
        dao.insert(mod);

        Optional<Mod> found = dao.findById(2);
        assertTrue(found.isPresent());
        assertEquals("implicit", found.get().getModType());
        assertNotNull(found.get().getStats());
    }

    @Test
    @DisplayName("insert enchant mod")
    void shouldInsertEnchant() {
        Mod mod = new Mod(3, "Enchantment Arc Chains", "3.24");
        mod.setModType("enchant");
        mod.setDomain("helmet");
        dao.insert(mod);

        Optional<Mod> found = dao.findById(3);
        assertTrue(found.isPresent());
        assertEquals("enchant", found.get().getModType());
        assertEquals("helmet", found.get().getDomain());
    }

    @Test
    @DisplayName("findAll returns all mods")
    void shouldFindAll() {
        dao.insert(new Mod(1, "Mod_1", "3.24"));
        dao.insert(new Mod(2, "Mod_2", "3.24"));
        assertEquals(2, dao.findAll().size());
    }

    @Test
    @DisplayName("count returns correct count")
    void shouldCount() {
        assertEquals(0, dao.count());
        dao.insert(new Mod(1, "Test", "3.24"));
        assertEquals(1, dao.count());
    }

    @Test
    @DisplayName("deleteById removes mod")
    void shouldDeleteById() {
        dao.insert(new Mod(1, "Test", "3.24"));
        dao.deleteById(1);
        assertEquals(0, dao.count());
    }

    @Test
    @DisplayName("findById returns empty for missing")
    void shouldReturnEmptyForMissing() {
        assertFalse(dao.findById(99999).isPresent());
    }

    @Test
    @DisplayName("batch insert 1000 mods under 1 second")
    void shouldBatchInsertWithinOneSecond() {
        List<Mod> mods = new ArrayList<>();
        for (int i = 1; i <= 1000; i++) {
            mods.add(new Mod(i, "Mod_" + i, "3.24"));
        }

        long start = System.currentTimeMillis();
        dao.batchInsert(mods);
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
