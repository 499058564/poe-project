package com.poe.cache.dao;

import com.poe.cache.model.PassiveSkill;
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

class PassiveSkillDaoTest {

    private Connection connection;
    private PassiveSkillDao dao;

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
        dao = new PassiveSkillDao(connection);
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
        PassiveSkill skill = new PassiveSkill(1, "Heart of the Warrior", "3.24");
        skill.setKeystone(true);
        skill.setX(100.5);
        skill.setY(200.3);
        dao.insert(skill);

        Optional<PassiveSkill> found = dao.findById(1);
        assertTrue(found.isPresent());
        assertEquals("Heart of the Warrior", found.get().getName());
        assertTrue(found.get().isKeystone());
        assertEquals(100.5, found.get().getX(), 0.001);
        assertEquals(200.3, found.get().getY(), 0.001);
    }

    @Test
    @DisplayName("insert notable skill")
    void shouldInsertNotable() {
        PassiveSkill skill = new PassiveSkill(2, "Precision", "3.24");
        skill.setNotable(true);
        dao.insert(skill);

        Optional<PassiveSkill> found = dao.findById(2);
        assertTrue(found.isPresent());
        assertTrue(found.get().isNotable());
        assertFalse(found.get().isKeystone());
    }

    @Test
    @DisplayName("insert jewel socket")
    void shouldInsertJewelSocket() {
        PassiveSkill skill = new PassiveSkill(3, "Jewel Socket", "3.24");
        skill.setJewelSocket(true);
        dao.insert(skill);

        Optional<PassiveSkill> found = dao.findById(3);
        assertTrue(found.isPresent());
        assertTrue(found.get().isJewelSocket());
    }

    @Test
    @DisplayName("insert ascendancy skill")
    void shouldInsertAscendancy() {
        PassiveSkill skill = new PassiveSkill(4, "Inevitable Judgement", "3.24");
        skill.setAscendancy("Inquisitor");
        skill.setPassiveClass("Ascendancy");
        dao.insert(skill);

        Optional<PassiveSkill> found = dao.findById(4);
        assertTrue(found.isPresent());
        assertEquals("Inquisitor", found.get().getAscendancy());
    }

    @Test
    @DisplayName("findAll returns all skills")
    void shouldFindAll() {
        dao.insert(new PassiveSkill(1, "Skill_1", "3.24"));
        dao.insert(new PassiveSkill(2, "Skill_2", "3.24"));
        assertEquals(2, dao.findAll().size());
    }

    @Test
    @DisplayName("count returns correct count")
    void shouldCount() {
        assertEquals(0, dao.count());
        dao.insert(new PassiveSkill(1, "Test", "3.24"));
        assertEquals(1, dao.count());
    }

    @Test
    @DisplayName("deleteById removes skill")
    void shouldDeleteById() {
        dao.insert(new PassiveSkill(1, "Test", "3.24"));
        dao.deleteById(1);
        assertEquals(0, dao.count());
    }

    @Test
    @DisplayName("findById returns empty for missing")
    void shouldReturnEmptyForMissing() {
        assertFalse(dao.findById(99999).isPresent());
    }

    @Test
    @DisplayName("batch insert 1000 skills under 1 second")
    void shouldBatchInsertWithinOneSecond() {
        List<PassiveSkill> skills = new ArrayList<>();
        for (int i = 1; i <= 1000; i++) {
            skills.add(new PassiveSkill(i, "Skill_" + i, "3.24"));
        }

        long start = System.currentTimeMillis();
        dao.batchInsert(skills);
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
