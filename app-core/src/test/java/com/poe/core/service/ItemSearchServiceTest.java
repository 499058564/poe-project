package com.poe.core.service;

import com.poe.cache.dao.ItemDao;
import com.poe.cache.dao.SearchDao;
import com.poe.cache.dao.TranslationDao;
import com.poe.cache.model.Item;
import com.poe.cache.model.ItemSummary;
import com.poe.common.util.StringUtils;
import com.poe.core.model.ItemDetail;
import com.poe.core.model.ModLine;
import com.poe.core.model.SearchResult;
import org.junit.jupiter.api.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import javax.sql.DataSource;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ItemSearchServiceTest {

    private Connection connection;
    private javax.sql.DataSource dataSource;
    private ItemSearchService service;
    private ItemDao itemDao;
    private SearchDao searchDao;
    private TranslationDao translationDao;
    private TranslationService translationService;

    private static final String[] MIGRATION_FILES = {
        "v001_base_items.sql",
        "v007_items_fts.sql",
        "v006_translations.sql",
    };

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite:file::memory:?cache=shared");
        org.sqlite.SQLiteDataSource ds = new org.sqlite.SQLiteDataSource();
        ds.setUrl("jdbc:sqlite:file::memory:?cache=shared");
        dataSource = ds;
        runMigrations();
        insertTestData();

        itemDao = new ItemDao(dataSource);
        searchDao = new SearchDao(dataSource);
        translationDao = new TranslationDao(dataSource);
        translationService = new TranslationService(translationDao);
        service = new ItemSearchService(searchDao, itemDao, translationService);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    // ── search 测试 ──

    @Test
    @DisplayName("search by English name returns matching items")
    void shouldSearchByEnglishName() {
        SearchResult<ItemSummary> result = service.search("Mageblood", 1, 50);

        assertEquals(1, result.getTotal());
        assertEquals(1, result.getItems().size());
        assertEquals("Mageblood", result.getItems().get(0).getName());
        assertEquals("法师之血", result.getItems().get(0).getNameZh());
    }

    @Test
    @DisplayName("search by Chinese name returns same result")
    void shouldSearchByChineseName() {
        SearchResult<ItemSummary> result = service.search("法师之血", 1, 50);

        assertEquals(1, result.getTotal());
        assertEquals("Mageblood", result.getItems().get(0).getName());
    }

    @Test
    @DisplayName("search with class filter returns only matching class")
    void shouldFilterByClass() {
        SearchResult<ItemSummary> result = service.search("Iron", "Ring", 1, 50);

        assertEquals(1, result.getTotal());
        assertEquals("Iron Ring", result.getItems().get(0).getName());
    }

    @Test
    @DisplayName("search pagination returns different data on page 2")
    void shouldPaginate() {
        SearchResult<ItemSummary> page1 = service.search("Ring", 1, 1);
        SearchResult<ItemSummary> page2 = service.search("Ring", 2, 1);

        assertEquals(1, page1.getItems().size());
        assertEquals(1, page2.getItems().size());
        assertEquals(2, page1.getTotal());

        String name1 = page1.getItems().get(0).getName();
        String name2 = page2.getItems().get(0).getName();
        assertNotEquals(name1, name2, "Page 1 and page 2 should have different data");
    }

    @Test
    @DisplayName("empty keyword returns empty result")
    void shouldReturnEmptyForBlankKeyword() {
        SearchResult<ItemSummary> result = service.search("", 1, 50);
        assertTrue(result.getItems().isEmpty());
        assertEquals(0, result.getTotal());

        result = service.search(null, 1, 50);
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    @DisplayName("search with no match returns empty")
    void shouldReturnEmptyForNoMatch() {
        SearchResult<ItemSummary> result = service.search("NonexistentXYZ123", 1, 50);
        assertTrue(result.getItems().isEmpty());
        assertEquals(0, result.getTotal());
    }

    @Test
    @DisplayName("search result has correct pagination metadata")
    void shouldHaveCorrectPagination() {
        SearchResult<ItemSummary> result = service.search("Ring", 1, 1);

        assertEquals(1, result.getPage());
        assertEquals(1, result.getPageSize());
        assertEquals(2, result.getTotal());
        assertEquals(2, result.getTotalPages());
        assertTrue(result.isHasMore());
    }

    @Test
    @DisplayName("search result on last page hasMore is false")
    void shouldShowNoMoreOnLastPage() {
        SearchResult<ItemSummary> result = service.search("Ring", 2, 1);
        assertFalse(result.isHasMore());
    }

    // ── getItemDetail 测试 ──

    @Test
    @DisplayName("getItemDetail returns full item details")
    void shouldGetItemDetail() {
        ItemDetail detail = service.getItemDetail(1);

        assertNotNull(detail);
        assertEquals("Iron Hat", detail.getSummary().getName());
        assertEquals("铁盔", detail.getSummary().getNameZh());
        assertEquals("Helmet", detail.getSummary().getItemClass());
        assertEquals(10, detail.getSummary().getDropLevel());
    }

    @Test
    @DisplayName("getItemDetail returns null for non-existing item")
    void shouldReturnNullForNonExistingItem() {
        ItemDetail detail = service.getItemDetail(9999);
        assertNull(detail);
    }

    @Test
    @DisplayName("getItemDetail parses implicits")
    void shouldParseImplicits() {
        ItemDetail detail = service.getItemDetail(1);

        List<String> implicits = detail.getImplicits();
        assertFalse(implicits.isEmpty());
        assertTrue(implicits.stream().anyMatch(s -> s.contains("maximum Life")));
    }

    @Test
    @DisplayName("getItemDetail parses requirements")
    void shouldParseRequirements() {
        ItemDetail detail = service.getItemDetail(1);

        Map<String, Integer> reqs = detail.getRequirements();
        assertFalse(reqs.isEmpty());
        assertTrue(reqs.containsKey("str"));
        assertTrue(reqs.get("str") > 0);
    }

    // ── parseImplicits / parseRequirements 单元测试 ──

    @Test
    @DisplayName("parseImplicits extracts text fields from JSON array")
    void shouldParseImplicitsJson() {
        String json = "[{\"text\":\"+20 to maximum Life\"},{\"text\":\"+10 to Strength\"}]";
        List<String> result = service.parseImplicits(json);

        assertEquals(2, result.size());
        assertEquals("+20 to maximum Life", result.get(0));
        assertEquals("+10 to Strength", result.get(1));
    }

    @Test
    @DisplayName("parseImplicits returns empty for null or blank")
    void shouldReturnEmptyForNullOrBlankImplicits() {
        assertTrue(service.parseImplicits(null).isEmpty());
        assertTrue(service.parseImplicits("").isEmpty());
    }

    @Test
    @DisplayName("parseRequirements extracts name-value pairs")
    void shouldParseRequirementsJson() {
        String json = "[{\"name\":\"str\",\"values\":[[\"100\"]]},{\"name\":\"dex\",\"values\":[[\"50\"]]}]";
        Map<String, Integer> result = service.parseRequirements(json);

        assertEquals(2, result.size());
        assertEquals(100, (int) result.get("str"));
        assertEquals(50, (int) result.get("dex"));
    }

    @Test
    @DisplayName("parseRequirements returns empty for null or blank")
    void shouldReturnEmptyForNullOrBlankRequirements() {
        assertTrue(service.parseRequirements(null).isEmpty());
        assertTrue(service.parseRequirements("").isEmpty());
    }

    // ── 辅助方法 ──

    private void insertTestData() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            // base_items 含 implicits、requirements
            stmt.executeUpdate(
                "INSERT INTO base_items (id, name, name_zh, class, drop_level, wiki_url, " +
                "requirements, implicits, version) VALUES " +
                "(1, 'Iron Hat', '铁盔', 'Helmet', 10, 'https://poewiki.net/wiki/Iron_Hat', " +
                "'[{\"name\":\"str\",\"values\":[[\"50\"]]}]', " +
                "'[{\"text\":\"+20 to maximum Life\"},{\"text\":\"+10 to Strength\"}]', '3.24')"
            );
            stmt.executeUpdate(
                "INSERT INTO base_items (id, name, name_zh, class, drop_level, version) VALUES " +
                "(2, 'Mageblood', '法师之血', 'Belt', 94, '3.24')"
            );
            stmt.executeUpdate(
                "INSERT INTO base_items (id, name, name_zh, class, drop_level, version) VALUES " +
                "(3, 'Iron Ring', '铁戒指', 'Ring', 1, '3.24')"
            );
            stmt.executeUpdate(
                "INSERT INTO base_items (id, name, name_zh, class, drop_level, version) VALUES " +
                "(4, 'Gold Ring', '金戒指', 'Ring', 1, '3.24')"
            );
            stmt.executeUpdate(
                "INSERT INTO base_items (id, name, name_zh, class, drop_level, version) VALUES " +
                "(5, 'Staff of Power', '力量法杖', 'Weapon', 30, '3.24')"
            );
            // 重建 FTS 索引
            stmt.executeUpdate("INSERT INTO items_fts(items_fts) VALUES ('rebuild')");
        }
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
