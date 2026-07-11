package com.poe.cache.dao;

import com.poe.cache.model.*;
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

/**
 * 词缀/工艺/经济数据 DAO 单元测试（内存 SQLite）。
 * 使用 v001～v009 迁移脚本初始化表结构。
 */
class ModCraftingDaoTest {

    private Connection connection;

    private static final String[] MIGRATION_FILES = {
        "v001_base_items.sql",
        "v002_skill_gems.sql",
        "v003_passive_skills.sql",
        "v004_mods.sql",
        "v005_data_version.sql",
        "v006_translations.sql",
        "v007_items_fts.sql",
        "v008_equipment_subtables.sql",
        "v009_mods_crafting_economy.sql",
    };

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        runMigrations();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    // ==================== 词缀子表 ====================

    @Test
    @DisplayName("ModStatDao: insert and findById")
    void modStatDaoInsertAndFindById() {
        ModStatDao dao = new ModStatDao(connection);
        ModStat ms = new ModStat();
        ms.setPageId(100);
        ms.setPageName("TestMod");
        ms.setStatId("stat_1");
        ms.setMinValue(10);
        ms.setMaxValue(20);
        dao.insert(ms);

        Optional<ModStat> found = dao.findById(100);
        assertTrue(found.isPresent());
        assertEquals("stat_1", found.get().getStatId());
        assertEquals(10, found.get().getMinValue());
        assertEquals(20, found.get().getMaxValue());
    }

    @Test
    @DisplayName("ModSpawnWeightDao: batchInsert and count")
    void modSpawnWeightDaoBatchInsert() {
        ModSpawnWeightDao dao = new ModSpawnWeightDao(connection);
        List<ModSpawnWeight> list = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            ModSpawnWeight msw = new ModSpawnWeight();
            msw.setPageId(200);
            msw.setPageName("MSW_" + i);
            msw.setOrdinal(i);
            msw.setTag("tag_" + i);
            msw.setValue(100);
            list.add(msw);
        }
        dao.batchInsert(list);
        assertEquals(50, dao.count());
    }

    @Test
    @DisplayName("ModSellPriceDao: insert and findById")
    void modSellPriceDaoInsertAndFindById() {
        ModSellPriceDao dao = new ModSellPriceDao(connection);
        ModSellPrice msp = new ModSellPrice();
        msp.setPageId(300);
        msp.setPageName("TestMod");
        msp.setAmount(3);
        msp.setCurrencyName("Orb of Alteration");
        dao.insert(msp);

        Optional<ModSellPrice> found = dao.findById(300);
        assertTrue(found.isPresent());
        assertEquals(3, found.get().getAmount());
        assertEquals("Orb of Alteration", found.get().getCurrencyName());
    }

    // ==================== 物品-词缀关联 ====================

    @Test
    @DisplayName("ItemModDao: insert with boolean fields")
    void itemModDaoInsertWithBooleanFields() {
        ItemModDao dao = new ItemModDao(connection);
        ItemMod im = new ItemMod();
        im.setPageId(400);
        im.setPageName("TestItem");
        im.setModId("mod_42");
        im.setExplicit(true);
        im.setImplicit(false);
        im.setMapFragmentBonus(false);
        im.setRandom(true);
        im.setText("+10 to Strength");
        dao.insert(im);

        Optional<ItemMod> found = dao.findById(400);
        assertTrue(found.isPresent());
        assertTrue(found.get().isExplicit());
        assertFalse(found.get().isImplicit());
        assertEquals("+10 to Strength", found.get().getText());
    }

    @Test
    @DisplayName("ItemStatDao: insert and findById")
    void itemStatDaoInsertAndFindById() {
        ItemStatDao dao = new ItemStatDao(connection);
        ItemStat is = new ItemStat();
        is.setPageId(500);
        is.setPageName("TestItem");
        is.setStatId("stat_7");
        is.setMinValue(10);
        is.setMaxValue(20);
        is.setAvg(15);
        is.setModId("mod_99");
        dao.insert(is);

        Optional<ItemStat> found = dao.findById(500);
        assertTrue(found.isPresent());
        assertEquals("stat_7", found.get().getStatId());
        assertEquals(10, found.get().getMinValue());
        assertEquals(20, found.get().getMaxValue());
    }

    @Test
    @DisplayName("ItemBuffDao: insert with JSON buff_values")
    void itemBuffDaoInsertWithJsonValues() {
        ItemBuffDao dao = new ItemBuffDao(connection);
        ItemBuff ib = new ItemBuff();
        ib.setPageId(600);
        ib.setPageName("TestBuff");
        ib.setBuffId("buff_3");
        ib.setIcon("BuffIcon.png");
        ib.setStatText("+20% Fire Resistance");
        ib.setBuffValues("[{\"key\":\"value1\"}]");
        dao.insert(ib);

        Optional<ItemBuff> found = dao.findById(600);
        assertTrue(found.isPresent());
        assertEquals("buff_3", found.get().getBuffId());
        assertEquals("+20% Fire Resistance", found.get().getStatText());
        assertNotNull(found.get().getBuffValues());
    }

    // ==================== 工艺/配方 ====================

    @Test
    @DisplayName("CraftingBenchOptionDao: insert and findById")
    void craftingBenchOptionDaoInsertAndFindById() {
        CraftingBenchOptionDao dao = new CraftingBenchOptionDao(connection);
        CraftingBenchOption cbo = new CraftingBenchOption();
        cbo.setOptionId(1);
        cbo.setPageId(700);
        cbo.setPageName("Craft_1");
        cbo.setName("+10 to maximum Life");
        cbo.setAffixType("Prefix");
        cbo.setModId("ModCraftedLife");
        cbo.setRank(1);
        cbo.setRequiredLevel(30);
        cbo.setNpc("Elreon");
        cbo.setOrdinal(1);
        cbo.setSockets(0);
        cbo.setUnveilsRequired(0);
        dao.insert(cbo);

        Optional<CraftingBenchOption> found = dao.findById(1);
        assertTrue(found.isPresent());
        assertEquals("+10 to maximum Life", found.get().getName());
        assertEquals("Prefix", found.get().getAffixType());
        assertEquals(30, found.get().getRequiredLevel());
    }

    @Test
    @DisplayName("CraftingBenchOptionCostDao: insert and findById")
    void craftingBenchOptionCostDaoInsertAndFindById() {
        CraftingBenchOptionCostDao dao = new CraftingBenchOptionCostDao(connection);
        CraftingBenchOptionCost cost = new CraftingBenchOptionCost();
        cost.setPageId(701);
        cost.setPageName("Cost_1");
        cost.setOptionId(1);
        cost.setAmount(5);
        cost.setCurrencyName("Orb of Fusing");
        dao.insert(cost);

        Optional<CraftingBenchOptionCost> found = dao.findById(701);
        assertTrue(found.isPresent());
        assertEquals(5, found.get().getAmount());
        assertEquals("Orb of Fusing", found.get().getCurrencyName());
    }

    @Test
    @DisplayName("EssenceDao: insert and findById")
    void essenceDaoInsertAndFindById() {
        EssenceDao dao = new EssenceDao(connection);
        Essence e = new Essence();
        e.setPageId(800);
        e.setPageName("Essence_of_Greed");
        e.setCategory("Attribute");
        e.setLevel(35);
        e.setLevelRestriction(35);
        e.setType(1);
        dao.insert(e);

        Optional<Essence> found = dao.findById(800);
        assertTrue(found.isPresent());
        assertEquals("Attribute", found.get().getCategory());
        assertEquals(35, found.get().getLevel());
        assertEquals(1, found.get().getType());
    }

    @Test
    @DisplayName("FossilDao: insert with boolean and JSON fields")
    void fossilDaoInsertWithBooleanFields() {
        FossilDao dao = new FossilDao(connection);
        Fossil f = new Fossil();
        f.setPageId(900);
        f.setPageName("Aberrant_Fossil");
        f.setCanEnchant(true);
        f.setCanMirror(false);
        f.setCanQuality(true);
        f.setCanRollWhiteSockets(false);
        f.setLucky(false);
        f.setCorruptedEssenceChance(250);
        f.setAllowedTags("[\"life\",\"mana\"]");
        dao.insert(f);

        Optional<Fossil> found = dao.findById(900);
        assertTrue(found.isPresent());
        assertTrue(found.get().isCanEnchant());
        assertFalse(found.get().isCanMirror());
        assertEquals(250, found.get().getCorruptedEssenceChance());
    }

    @Test
    @DisplayName("FossilWeightDao: insert and batchInsert")
    void fossilWeightDaoBatchInsert() {
        FossilWeightDao dao = new FossilWeightDao(connection);
        List<FossilWeight> list = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            FossilWeight fw = new FossilWeight();
            fw.setPageId(1000);
            fw.setPageName("FW_" + i);
            fw.setBaseItemId("item_" + (i % 5));
            fw.setOrdinal(i);
            fw.setTag("tag_" + i);
            fw.setWeightType("weight");
            fw.setWeight(100);
            list.add(fw);
        }
        dao.batchInsert(list);
        assertEquals(20, dao.count());
    }

    // ==================== 经济数据 ====================

    @Test
    @DisplayName("VendorRewardDao: insert with JSON class_ids")
    void vendorRewardDaoInsertWithJson() {
        VendorRewardDao dao = new VendorRewardDao(connection);
        VendorReward vr = new VendorReward();
        vr.setPageId(1100);
        vr.setPageName("Recipe_1");
        vr.setAct(1);
        vr.setClassIds("[\"marauder\",\"duelist\"]");
        vr.setClasses("Marauder, Duelist");
        vr.setNpc("Nessa");
        vr.setQuest("Breaking Some Eggs");
        vr.setQuestId("a1q1");
        dao.insert(vr);

        Optional<VendorReward> found = dao.findById(1100);
        assertTrue(found.isPresent());
        assertEquals(1, found.get().getAct());
        assertEquals("Nessa", found.get().getNpc());
        assertEquals("Breaking Some Eggs", found.get().getQuest());
    }

    @Test
    @DisplayName("ItemSellPriceDao: insert and findById")
    void itemSellPriceDaoInsertAndFindById() {
        ItemSellPriceDao dao = new ItemSellPriceDao(connection);
        ItemSellPrice isp = new ItemSellPrice();
        isp.setPageId(1200);
        isp.setPageName("SellPrice_1");
        isp.setAmount(5);
        isp.setCurrencyName("Scroll Fragment");
        dao.insert(isp);

        Optional<ItemSellPrice> found = dao.findById(1200);
        assertTrue(found.isPresent());
        assertEquals(5, found.get().getAmount());
        assertEquals("Scroll Fragment", found.get().getCurrencyName());
    }

    @Test
    @DisplayName("ItemPurchaseCostDao: insert with rarity field")
    void itemPurchaseCostDaoInsertWithRarity() {
        ItemPurchaseCostDao dao = new ItemPurchaseCostDao(connection);
        ItemPurchaseCost ipc = new ItemPurchaseCost();
        ipc.setPageId(1300);
        ipc.setPageName("Cost_1");
        ipc.setAmount(1);
        ipc.setCurrencyName("Orb of Transmutation");
        ipc.setRarity("magic");
        dao.insert(ipc);

        Optional<ItemPurchaseCost> found = dao.findById(1300);
        assertTrue(found.isPresent());
        assertEquals(1, found.get().getAmount());
        assertEquals("Orb of Transmutation", found.get().getCurrencyName());
        assertEquals("magic", found.get().getRarity());
    }

    // ==================== 事务回滚 ====================

    @Test
    @DisplayName("ModSpawnWeightDao: batchInsert rolls back on duplicate")
    void modSpawnWeightDaoBatchInsertRollback() {
        ModSpawnWeightDao dao = new ModSpawnWeightDao(connection);
        List<ModSpawnWeight> list = new ArrayList<>();
        ModSpawnWeight msw1 = new ModSpawnWeight();
        msw1.setPageId(200);
        msw1.setPageName("Test_1");
        msw1.setOrdinal(0);
        msw1.setTag("ring");
        msw1.setValue(1000);
        list.add(msw1);
        ModSpawnWeight msw2 = new ModSpawnWeight();
        msw2.setPageId(200);
        msw2.setPageName("Test_2");
        msw2.setOrdinal(0);
        msw2.setTag("ring");
        msw2.setValue(2000);
        list.add(msw2);

        assertThrows(RuntimeException.class, () -> dao.batchInsert(list));
        assertEquals(0, dao.count());
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
}
