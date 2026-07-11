package com.poe.cache.dao;

import com.poe.cache.model.Item;
import org.junit.jupiter.api.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import javax.sql.DataSource;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ItemDaoTest {

    private Connection connection;
    private javax.sql.DataSource dataSource;
    private ItemDao dao;

    private static final String[] MIGRATION_FILES = {
        "v001_base_items.sql",
        "v002_skill_gems.sql",
        "v003_passive_skills.sql",
        "v004_mods.sql",
        "v005_data_version.sql",
        "v006_translations.sql",
        "v007_items_fts.sql",
    };

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite:file::memory:?cache=shared");
        dataSource = createDataSource();
        runMigrations();
        dao = new ItemDao(dataSource);
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
        Item item = new Item(1, "Iron Hat", "Helmet", "3.24");
        dao.insert(item);

        Optional<Item> found = dao.findById(1);
        assertTrue(found.isPresent());
        assertEquals("Iron Hat", found.get().getName());
        assertEquals("Helmet", found.get().getItemClass());
    }

    @Test
    @DisplayName("insert with all fields")
    void shouldInsertAllFields() {
        Item item = new Item();
        item.setId(100);
        item.setName("Tabula Rasa");
        item.setNameZh("白袍");
        item.setItemClass("Body Armour");
        item.setInventoryWidth(2);
        item.setInventoryHeight(3);
        item.setDropLevel(1);
        item.setVersion("3.24");
        item.setFlavourText("The easiest armour to find, yet the most difficult to wear.");

        dao.insert(item);

        Optional<Item> found = dao.findById(100);
        assertTrue(found.isPresent());
        assertEquals("Tabula Rasa", found.get().getName());
        assertEquals("白袍", found.get().getNameZh());
        assertEquals(2, found.get().getInventoryWidth());
        assertEquals(3, found.get().getInventoryHeight());
    }

    @Test
    @DisplayName("findById returns empty for missing")
    void shouldReturnEmptyForMissing() {
        Optional<Item> found = dao.findById(99999);
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("findAll returns all items")
    void shouldFindAll() {
        dao.insert(new Item(1, "Iron Hat", "Helmet", "3.24"));
        dao.insert(new Item(2, "Rustic Sash", "Belt", "3.24"));
        dao.insert(new Item(3, "Gold Ring", "Ring", "3.24"));

        List<Item> all = dao.findAll();
        assertEquals(3, all.size());
    }

    @Test
    @DisplayName("count returns correct count")
    void shouldCount() {
        assertEquals(0, dao.count());
        dao.insert(new Item(1, "Item1", "Test", "3.24"));
        assertEquals(1, dao.count());
        dao.insert(new Item(2, "Item2", "Test", "3.24"));
        assertEquals(2, dao.count());
    }

    @Test
    @DisplayName("deleteById removes item")
    void shouldDeleteById() {
        dao.insert(new Item(1, "Iron Hat", "Helmet", "3.24"));
        assertEquals(1, dao.count());

        dao.deleteById(1);
        assertEquals(0, dao.count());
        assertFalse(dao.findById(1).isPresent());
    }

    @Test
    @DisplayName("batch insert 1000 items under 1 second")
    void shouldBatchInsertWithinOneSecond() {
        List<Item> items = new ArrayList<>();
        for (int i = 1; i <= 1000; i++) {
            items.add(new Item(i, "Item_" + i, "Test", "3.24"));
        }

        long start = System.currentTimeMillis();
        dao.batchInsert(items);
        long elapsed = System.currentTimeMillis() - start;

        assertEquals(1000, dao.count());
        assertTrue(elapsed < 1000, "Batch insert took " + elapsed + "ms, expected < 1000ms");
    }

    @Test
    @DisplayName("batch insert rolls back on error")
    void shouldRollbackOnError() {
        List<Item> items = new ArrayList<>();
        items.add(new Item(1, "Item_1", "Test", "3.24"));
        items.add(new Item(1, "Item_2", "Test", "3.24")); // duplicate id

        assertThrows(RuntimeException.class, () -> dao.batchInsert(items));
        assertEquals(0, dao.count()); // rollback should prevent any insert
    }

    // ---- helpers ----

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

    private javax.sql.DataSource createDataSource() {
        org.sqlite.SQLiteDataSource ds = new org.sqlite.SQLiteDataSource();
        ds.setUrl("jdbc:sqlite:file::memory:?cache=shared");
        return ds;
    }}
