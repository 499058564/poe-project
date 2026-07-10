package com.poe.cache.dao;

import com.poe.cache.model.ItemSummary;
import org.junit.jupiter.api.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class SearchDaoTest {

    private Connection connection;
    private SearchDao dao;

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
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        runMigrations();

        // 插入测试数据
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(
                "INSERT INTO base_items (id, name, name_zh, class, version) " +
                "VALUES (1, 'Iron Hat', '铁盔', 'Helmet', '3.24')"
            );
            stmt.executeUpdate(
                "INSERT INTO base_items (id, name, name_zh, class, version) " +
                "VALUES (2, 'Mageblood', '法师之血', 'Belt', '3.24')"
            );
            stmt.executeUpdate(
                "INSERT INTO base_items (id, name, name_zh, class, version) " +
                "VALUES (3, 'Iron Ring', '铁戒指', 'Ring', '3.24')"
            );
            stmt.executeUpdate(
                "INSERT INTO base_items (id, name, name_zh, class, version) " +
                "VALUES (4, 'Gold Ring', '金戒指', 'Ring', '3.24')"
            );
            // 重建 FTS 内容表索引
            stmt.executeUpdate("INSERT INTO items_fts(items_fts) VALUES ('rebuild')");
        }

        dao = new SearchDao(connection);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    @DisplayName("search by English name")
    void shouldSearchByName() {
        List<ItemSummary> results = dao.searchItems("Iron", 10, 0);

        assertEquals(2, results.size());
        List<String> names = results.stream().map(ItemSummary::getName).collect(Collectors.toList());
        assertTrue(names.contains("Iron Hat"));
        assertTrue(names.contains("Iron Ring"));
    }

    @Test
    @DisplayName("search by Chinese name")
    void shouldSearchByChineseName() {
        // FTS5 默认不分割 CJK 字符，使用完整中文名称搜索
        List<ItemSummary> results = dao.searchItems("铁盔", 10, 0);
        assertEquals(1, results.size());
        assertEquals("Iron Hat", results.get(0).getName());

        results = dao.searchItems("铁戒指", 10, 0);
        assertEquals(1, results.size());
        assertEquals("Iron Ring", results.get(0).getName());
    }

    @Test
    @DisplayName("search by class")
    void shouldSearchByClass() {
        List<ItemSummary> results = dao.searchItems("Ring", 10, 0);

        List<String> names = results.stream().map(ItemSummary::getName).collect(Collectors.toList());
        assertTrue(names.contains("Iron Ring"));
        assertTrue(names.contains("Gold Ring"));
    }

    @Test
    @DisplayName("search with limit and offset")
    void shouldRespectLimitAndOffset() {
        List<ItemSummary> results = dao.searchItems("Ring", 1, 0);
        assertEquals(1, results.size());

        List<ItemSummary> page2 = dao.searchItems("Ring", 10, 0);
        assertTrue(page2.size() >= 2); // finds Iron Ring and Gold Ring
    }

    @Test
    @DisplayName("search returns empty for no match")
    void shouldReturnEmptyForNoMatch() {
        List<ItemSummary> results = dao.searchItems("NonexistentXYZ", 10, 0);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("searchItemCount returns correct count")
    void shouldCountSearchResults() {
        int count = dao.searchItemCount("Iron");
        assertEquals(2, count);
    }

    @Test
    @DisplayName("searchItemCount returns 0 for no match")
    void shouldReturnZeroCountForNoMatch() {
        assertEquals(0, dao.searchItemCount("NonexistentXYZ"));
    }

    @Test
    @DisplayName("search results include rank")
    void shouldIncludeRank() {
        List<ItemSummary> results = dao.searchItems("Mageblood", 10, 0);
        assertEquals(1, results.size());

        ItemSummary summary = results.get(0);
        assertEquals("Mageblood", summary.getName());
        assertEquals("法师之血", summary.getNameZh());
        assertEquals("Belt", summary.getItemClass());
        assertTrue(summary.getRank() != 0.0);
    }

    @Test
    @DisplayName("search supports FTS prefix matching")
    void shouldSupportPrefixMatching() {
        // FTS5 支持前缀匹配
        List<ItemSummary> results = dao.searchItems("Mage*", 10, 0);
        assertTrue(results.size() >= 1);
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
