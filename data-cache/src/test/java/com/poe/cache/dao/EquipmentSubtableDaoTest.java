package com.poe.cache.dao;

import com.poe.cache.model.*;
import org.junit.jupiter.api.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import javax.sql.DataSource;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 所有装备子表 DAO 的单元测试（内存 SQLite）。
 */
class EquipmentSubtableDaoTest {

    private Connection connection;
    private javax.sql.DataSource dataSource;

    private static final String[] MIGRATION_FILES = {
        "v001_base_items.sql",
        "v002_skill_gems.sql",
        "v003_passive_skills.sql",
        "v004_mods.sql",
        "v005_data_version.sql",
        "v006_translations.sql",
        "v007_items_fts.sql",
        "v008_equipment_subtables.sql",
    };

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite:file::memory:?cache=shared");
        dataSource = createDataSource();
        runMigrations();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    // ==================== WeaponDao ====================

    @Test
    @DisplayName("WeaponDao: insert and findById")
    void weaponDaoInsertAndFindById() {
        WeaponDao dao = new WeaponDao(dataSource);
        Weapon w = new Weapon();
        w.setPageId(1);
        w.setPageName("Rusted_Sword");
        w.setAttackSpeed(1.45);
        w.setCriticalStrikeChance(5.0);
        w.setWeaponRange(11.0);
        w.setPhysicalDamageMin(5);
        w.setPhysicalDamageMax(12);
        dao.insert(w);

        Optional<Weapon> found = dao.findById(1);
        assertTrue(found.isPresent());
        assertEquals("Rusted_Sword", found.get().getPageName());
        assertEquals(1.45, found.get().getAttackSpeed(), 0.001);
        assertEquals(5, found.get().getPhysicalDamageMin());
        assertEquals(12, found.get().getPhysicalDamageMax());
    }

    @Test
    @DisplayName("WeaponDao: batchInsert and count")
    void weaponDaoBatchInsert() {
        WeaponDao dao = new WeaponDao(dataSource);
        List<Weapon> weapons = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            Weapon w = new Weapon();
            w.setPageId(i);
            w.setPageName("Weapon_" + i);
            w.setPhysicalDamageMin(i * 2);
            w.setPhysicalDamageMax(i * 3);
            weapons.add(w);
        }
        dao.batchInsert(weapons);
        assertEquals(100, dao.count());
    }

    @Test
    @DisplayName("WeaponDao: batchInsert rolls back on duplicate")
    void weaponDaoBatchInsertRollback() {
        WeaponDao dao = new WeaponDao(dataSource);
        List<Weapon> weapons = new ArrayList<>();
        Weapon w1 = new Weapon();
        w1.setPageId(1);
        w1.setPageName("A");
        weapons.add(w1);
        Weapon w2 = new Weapon();
        w2.setPageId(1);
        w2.setPageName("B");
        weapons.add(w2);

        assertThrows(RuntimeException.class, () -> dao.batchInsert(weapons));
        assertEquals(0, dao.count());
    }

    // ==================== ArmourDao ====================

    @Test
    @DisplayName("ArmourDao: insert and findById")
    void armourDaoInsertAndFindById() {
        ArmourDao dao = new ArmourDao(dataSource);
        Armour a = new Armour();
        a.setPageId(1);
        a.setPageName("Simple_Robe");
        a.setArmourMin(10);
        a.setArmourMax(15);
        a.setEnergyShieldMin(20);
        a.setEnergyShieldMax(25);
        a.setMovementSpeed(-3);
        dao.insert(a);

        Optional<Armour> found = dao.findById(1);
        assertTrue(found.isPresent());
        assertEquals(10, found.get().getArmourMin());
        assertEquals(25, found.get().getEnergyShieldMax());
        assertEquals(-3, found.get().getMovementSpeed());
    }

    @Test
    @DisplayName("ArmourDao: batchInsert and count")
    void armourDaoBatchInsert() {
        ArmourDao dao = new ArmourDao(dataSource);
        List<Armour> list = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            Armour a = new Armour();
            a.setPageId(i);
            a.setPageName("Armour_" + i);
            a.setEvasionMin(i * 10);
            a.setEvasionMax(i * 12);
            list.add(a);
        }
        dao.batchInsert(list);
        assertEquals(50, dao.count());
    }

    // ==================== ShieldDao ====================

    @Test
    @DisplayName("ShieldDao: insert and findById")
    void shieldDaoInsertAndFindById() {
        ShieldDao dao = new ShieldDao(dataSource);
        Shield s = new Shield();
        s.setPageId(1);
        s.setPageName("Buckler");
        s.setBlock(25);
        dao.insert(s);

        Optional<Shield> found = dao.findById(1);
        assertTrue(found.isPresent());
        assertEquals(25, found.get().getBlock());
    }

    @Test
    @DisplayName("ShieldDao: findAll returns ordered")
    void shieldDaoFindAll() {
        ShieldDao dao = new ShieldDao(dataSource);
        Shield s1 = new Shield();
        s1.setPageId(2);
        s1.setPageName("B");
        s1.setBlock(20);
        Shield s2 = new Shield();
        s2.setPageId(1);
        s2.setPageName("A");
        s2.setBlock(15);
        dao.insert(s1);
        dao.insert(s2);

        List<Shield> all = dao.findAll();
        assertEquals(2, all.size());
        assertEquals(1, all.get(0).getPageId());
    }

    // ==================== AmuletDao ====================

    @Test
    @DisplayName("AmuletDao: insert with talisman fields")
    void amuletDaoInsertTalisman() {
        AmuletDao dao = new AmuletDao(dataSource);
        Amulet a = new Amulet();
        a.setPageId(1);
        a.setPageName("Wereclaw_Talisman");
        a.setTalisman(true);
        a.setTalismanTier(2);
        dao.insert(a);

        Optional<Amulet> found = dao.findById(1);
        assertTrue(found.isPresent());
        assertTrue(found.get().isTalisman());
        assertEquals(2, found.get().getTalismanTier());
    }

    @Test
    @DisplayName("AmuletDao: deleteById")
    void amuletDaoDeleteById() {
        AmuletDao dao = new AmuletDao(dataSource);
        Amulet a = new Amulet();
        a.setPageId(1);
        a.setPageName("Test_Amulet");
        dao.insert(a);
        assertEquals(1, dao.count());
        dao.deleteById(1);
        assertEquals(0, dao.count());
    }

    // ==================== FlaskDao ====================

    @Test
    @DisplayName("FlaskDao: insert and findById")
    void flaskDaoInsertAndFindById() {
        FlaskDao dao = new FlaskDao(dataSource);
        Flask f = new Flask();
        f.setPageId(1);
        f.setPageName("Amethyst_Flask");
        f.setChargesMax(65);
        f.setChargesPerUse(35);
        f.setDuration(6.5);
        f.setLife(0);
        f.setMana(0);
        dao.insert(f);

        Optional<Flask> found = dao.findById(1);
        assertTrue(found.isPresent());
        assertEquals(65, found.get().getChargesMax());
        assertEquals(6.5, found.get().getDuration(), 0.01);
    }

    @Test
    @DisplayName("FlaskDao: batchInsert with life/mana flasks")
    void flaskDaoBatchInsert() {
        FlaskDao dao = new FlaskDao(dataSource);
        List<Flask> flasks = new ArrayList<>();
        Flask life = new Flask();
        life.setPageId(1);
        life.setPageName("Divine_Life_Flask");
        life.setLife(100);
        life.setChargesMax(30);
        flasks.add(life);
        Flask mana = new Flask();
        mana.setPageId(2);
        mana.setPageName("Divine_Mana_Flask");
        mana.setMana(80);
        mana.setChargesMax(30);
        flasks.add(mana);
        dao.batchInsert(flasks);
        assertEquals(2, dao.count());
    }

    // ==================== JewelDao ====================

    @Test
    @DisplayName("JewelDao: insert and findById")
    void jewelDaoInsertAndFindById() {
        JewelDao dao = new JewelDao(dataSource);
        Jewel j = new Jewel();
        j.setPageId(1);
        j.setPageName("Cobalt_Jewel");
        j.setJewelLimit("1");
        j.setRadiusHtml("<span class=\"radius-large\">Large</span>");
        dao.insert(j);

        Optional<Jewel> found = dao.findById(1);
        assertTrue(found.isPresent());
        assertEquals("1", found.get().getJewelLimit());
        assertNotNull(found.get().getRadiusHtml());
    }

    // ==================== StackableDao ====================

    @Test
    @DisplayName("StackableDao: insert and findById")
    void stackableDaoInsertAndFindById() {
        StackableDao dao = new StackableDao(dataSource);
        Stackable s = new Stackable();
        s.setPageId(1);
        s.setPageName("Chaos_Orb");
        s.setStackSize(10);
        s.setStackSizeCurrencyTab(5000);
        dao.insert(s);

        Optional<Stackable> found = dao.findById(1);
        assertTrue(found.isPresent());
        assertEquals(10, found.get().getStackSize());
        assertEquals(5000, found.get().getStackSizeCurrencyTab());
    }

    // ==================== MapDao ====================

    @Test
    @DisplayName("MapDao: insert and findById")
    void mapDaoInsertAndFindById() {
        MapDao dao = new MapDao(dataSource);
        GameMap m = new GameMap();
        m.setPageId(1);
        m.setPageName("Beach_Map");
        m.setAreaId("1_1_1");
        m.setAreaLevel(1);
        m.setGuildCharacter("A");
        m.setSeries("Original");
        m.setTier(1);
        dao.insert(m);

        Optional<GameMap> found = dao.findById(1);
        assertTrue(found.isPresent());
        assertEquals("Beach_Map", found.get().getPageName());
        assertEquals(1, found.get().getTier());
        assertEquals("Original", found.get().getSeries());
    }

    @Test
    @DisplayName("MapDao: batchInsert with unique area")
    void mapDaoBatchInsert() {
        MapDao dao = new MapDao(dataSource);
        List<GameMap> maps = new ArrayList<>();
        GameMap m = new GameMap();
        m.setPageId(1);
        m.setPageName("Map_1");
        m.setTier(1);
        m.setSeries("Original");
        m.setUniqueAreaId("unique_beach");
        m.setUniqueAreaLevel(68);
        maps.add(m);
        dao.batchInsert(maps);
        assertEquals(1, dao.count());

        Optional<GameMap> found = dao.findById(1);
        assertEquals("unique_beach", found.get().getUniqueAreaId());
        assertEquals(68, found.get().getUniqueAreaLevel());
    }

    // ==================== MapFragmentDao ====================

    @Test
    @DisplayName("MapFragmentDao: insert and findById")
    void mapFragmentDaoInsertAndFindById() {
        MapFragmentDao dao = new MapFragmentDao(dataSource);
        MapFragment mf = new MapFragment();
        mf.setPageId(1);
        mf.setPageName("Sacrifice_at_Dusk");
        mf.setMapFragmentLimit(1);
        dao.insert(mf);

        Optional<MapFragment> found = dao.findById(1);
        assertTrue(found.isPresent());
        assertEquals(1, found.get().getMapFragmentLimit());
    }

    // ==================== MapSeriesDao ====================

    @Test
    @DisplayName("MapSeriesDao: insert and findById")
    void mapSeriesDaoInsertAndFindById() {
        MapSeriesDao dao = new MapSeriesDao(dataSource);
        MapSeries ms = new MapSeries();
        ms.setPageId(1);
        ms.setPageName("Atlas_of_Worlds");
        ms.setSeriesId("original");
        ms.setName("Original");
        ms.setOrdinal(1);
        dao.insert(ms);

        Optional<MapSeries> found = dao.findById(1);
        assertTrue(found.isPresent());
        assertEquals("Original", found.get().getName());
        assertEquals(1, found.get().getOrdinal());
    }

    // ==================== DivinationCardDao ====================

    @Test
    @DisplayName("DivinationCardDao: insert and findById")
    void divinationCardDaoInsertAndFindById() {
        DivinationCardDao dao = new DivinationCardDao(dataSource);
        DivinationCard dc = new DivinationCard();
        dc.setPageId(1);
        dc.setPageName("The_Doctor");
        dc.setCardArt("The_Doctor_card_art.png");
        dc.setCardBackground("1,2,3");
        dao.insert(dc);

        Optional<DivinationCard> found = dao.findById(1);
        assertTrue(found.isPresent());
        assertEquals("The_Doctor_card_art.png", found.get().getCardArt());
    }

    @Test
    @DisplayName("DivinationCardDao: batchInsert multiple cards")
    void divinationCardDaoBatchInsert() {
        DivinationCardDao dao = new DivinationCardDao(dataSource);
        List<DivinationCard> cards = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            DivinationCard dc = new DivinationCard();
            dc.setPageId(i);
            dc.setPageName("Card_" + i);
            cards.add(dc);
        }
        dao.batchInsert(cards);
        assertEquals(10, dao.count());
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
