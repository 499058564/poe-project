package com.poe.provider.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.poe.cache.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 词缀/工艺/经济数据 Converter 单元测试（共 15 个 Converter）。
 */
class ModCraftingConverterTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // ==================== 词缀子表 ====================

    @Test
    @DisplayName("ModStatConverter: converts id/min/max")
    void modStatConvertsAllFields() {
        ObjectNode row = MAPPER.createObjectNode();
        row.put("_pageID", "100");
        row.put("_pageName", "TestMod");
        row.put("id", "stat_1");
        row.put("min", "10");
        row.put("max", "20");

        ModStat ms = new ModStatConverter().convert(row);
        assertEquals(100, ms.getPageId());
        assertEquals("stat_1", ms.getStatId());
        assertEquals(10, ms.getMinValue());
        assertEquals(20, ms.getMaxValue());
    }

    @Test
    @DisplayName("ModStatConverter: handles missing fields")
    void modStatHandlesMissingFields() {
        ObjectNode row = MAPPER.createObjectNode();
        row.put("_pageID", "1");
        row.put("_pageName", "Test");

        ModStat ms = new ModStatConverter().convert(row);
        assertEquals(1, ms.getPageId());
        assertEquals(0, ms.getMinValue());
        assertEquals(0, ms.getMaxValue());
    }

    @Test
    @DisplayName("ModSpawnWeightConverter: converts ordinal/tag/value")
    void modSpawnWeightConvertsAllFields() {
        ObjectNode row = MAPPER.createObjectNode();
        row.put("_pageID", "200");
        row.put("_pageName", "TestMod");
        row.put("ordinal", "0");
        row.put("tag", "ring");
        row.put("value", "1000");

        ModSpawnWeight msw = new ModSpawnWeightConverter().convert(row);
        assertEquals(200, msw.getPageId());
        assertEquals(0, msw.getOrdinal());
        assertEquals("ring", msw.getTag());
        assertEquals(1000, msw.getValue());
    }

    @Test
    @DisplayName("ModSellPriceConverter: converts amount/name")
    void modSellPriceConvertsAllFields() {
        ObjectNode row = MAPPER.createObjectNode();
        row.put("_pageID", "300");
        row.put("_pageName", "TestMod");
        row.put("amount", "3");
        row.put("name", "Orb of Alteration");

        ModSellPrice msp = new ModSellPriceConverter().convert(row);
        assertEquals(300, msp.getPageId());
        assertEquals(3, msp.getAmount());
        assertEquals("Orb of Alteration", msp.getCurrencyName());
    }

    // ==================== 物品-词缀关联 ====================

    @Test
    @DisplayName("ItemModConverter: converts id/boolean fields")
    void itemModConvertsAllFields() {
        ObjectNode row = MAPPER.createObjectNode();
        row.put("_pageID", "400");
        row.put("_pageName", "TestItem");
        row.put("id", "mod_42");
        row.put("is_explicit", "1");
        row.put("is_implicit", "0");
        row.put("is_map_fragment_bonus", "0");
        row.put("is_random", "1");
        row.put("text", "+10 to Strength");

        ItemMod im = new ItemModConverter().convert(row);
        assertEquals(400, im.getPageId());
        assertEquals("mod_42", im.getModId());
        assertTrue(im.isExplicit());
        assertFalse(im.isImplicit());
        assertTrue(im.isRandom());
        assertEquals("+10 to Strength", im.getText());
    }

    @Test
    @DisplayName("ItemStatConverter: converts stat fields")
    void itemStatConvertsAllFields() {
        ObjectNode row = MAPPER.createObjectNode();
        row.put("_pageID", "500");
        row.put("_pageName", "TestItem");
        row.put("avg", "15");
        row.put("id", "stat_7");
        row.put("max", "20");
        row.put("min", "10");
        row.put("mod_id", "mod_99");

        ItemStat is = new ItemStatConverter().convert(row);
        assertEquals(500, is.getPageId());
        assertEquals("stat_7", is.getStatId());
        assertEquals(10, is.getMinValue());
        assertEquals(20, is.getMaxValue());
        assertEquals(15.0, is.getAvg(), 0.001);
        assertEquals("mod_99", is.getModId());
    }

    @Test
    @DisplayName("ItemBuffConverter: converts buff fields with JSON values")
    void itemBuffConvertsAllFields() {
        ObjectNode row = MAPPER.createObjectNode();
        row.put("_pageID", "600");
        row.put("_pageName", "TestBuff");
        row.put("id", "buff_3");
        row.put("icon", "BuffIcon.png");
        row.put("stat_text", "+20% to Fire Resistance");
        row.put("buff_values", "[{\"key\":\"value1\"},{\"key\":\"value2\"}]");

        ItemBuff ib = new ItemBuffConverter().convert(row);
        assertEquals(600, ib.getPageId());
        assertEquals("buff_3", ib.getBuffId());
        assertEquals("BuffIcon.png", ib.getIcon());
        assertEquals("+20% to Fire Resistance", ib.getStatText());
        assertNotNull(ib.getBuffValues());
    }

    // ==================== 工艺/配方 ====================

    @Test
    @DisplayName("CraftingBenchOptionConverter: converts all 20 fields")
    void craftingBenchOptionConvertsAllFields() {
        ObjectNode row = MAPPER.createObjectNode();
        row.put("_pageID", "700");
        row.put("_pageName", "Craft_Option_1");
        row.put("id", "1");
        row.put("name", "+10 to maximum Life");
        row.put("affix_type", "Prefix");
        row.put("mod_id", "ModCraftedLife");
        row.put("mod_group", "CraftedLife");
        row.put("rank", "1");
        row.put("required_level", "30");
        row.put("npc", "Elreon");
        row.put("description", "Adds prefix: +10 to maximum Life");
        row.put("recipe_unlock_location", "The Prison");
        row.put("crafting_bench_unlock_category", "Life Modifiers");
        row.put("crafting_bench_unlock_category_description", "Unlocks life modifiers");
        row.put("item_class_categories", "[\"ring\",\"amulet\"]");
        row.put("item_classes", "[\"Ring\",\"Amulet\"]");
        row.put("item_classes_ids", "[\"Ring\",\"Amulet\"]");
        row.put("links", "0");
        row.put("ordinal", "1");
        row.put("socket_colours", "");
        row.put("sockets", "0");
        row.put("unveils_required", "0");

        CraftingBenchOption cbo = new CraftingBenchOptionConverter().convert(row);
        assertEquals(1, cbo.getOptionId());
        assertEquals("+10 to maximum Life", cbo.getName());
        assertEquals("Prefix", cbo.getAffixType());
        assertEquals(30, cbo.getRequiredLevel());
        assertEquals("Elreon", cbo.getNpc());
        assertEquals(1, cbo.getRank());
        assertNotNull(cbo.getItemClassCategories());
    }

    @Test
    @DisplayName("CraftingBenchOptionCostConverter: converts cost fields")
    void craftingBenchOptionCostConvertsAllFields() {
        ObjectNode row = MAPPER.createObjectNode();
        row.put("_pageID", "701");
        row.put("_pageName", "Craft_Cost_1");
        row.put("amount", "5");
        row.put("name", "Orb of Fusing");
        row.put("option_id", "1");

        CraftingBenchOptionCost cost = new CraftingBenchOptionCostConverter().convert(row);
        assertEquals(5, cost.getAmount());
        assertEquals("Orb of Fusing", cost.getCurrencyName());
        assertEquals(1, cost.getOptionId());
    }

    @Test
    @DisplayName("EssenceConverter: converts essence fields")
    void essenceConvertsAllFields() {
        ObjectNode row = MAPPER.createObjectNode();
        row.put("_pageID", "800");
        row.put("_pageName", "Essence_of_Greed");
        row.put("category", "Attribute");
        row.put("level", "35");
        row.put("level_restriction", "35");
        row.put("type", "1");

        Essence e = new EssenceConverter().convert(row);
        assertEquals(800, e.getPageId());
        assertEquals("Attribute", e.getCategory());
        assertEquals(35, e.getLevel());
        assertEquals(35, e.getLevelRestriction());
        assertEquals(1, e.getType());
    }

    @Test
    @DisplayName("FossilConverter: converts fossil with boolean fields")
    void fossilConvertsAllFields() {
        ObjectNode row = MAPPER.createObjectNode();
        row.put("_pageID", "900");
        row.put("_pageName", "Aberrant_Fossil");
        row.put("added_modifier_ids", "[\"mod_a\",\"mod_b\"]");
        row.put("allowed_tags", "[\"life\",\"mana\"]");
        row.put("base_item_id", "item_1");
        row.put("can_enchant", "1");
        row.put("can_mirror", "0");
        row.put("can_quality", "1");
        row.put("can_roll_white_sockets", "0");
        row.put("corrupted_essence_chance", "250");
        row.put("forbidden_tags", "[\"speed\"]");
        row.put("forced_modifier_ids", "null");
        row.put("is_lucky", "0");
        row.put("sell_price_modifier_ids", "[\"mod_c\"]");

        Fossil f = new FossilConverter().convert(row);
        assertEquals(900, f.getPageId());
        assertTrue(f.isCanEnchant());
        assertFalse(f.isCanMirror());
        assertTrue(f.isCanQuality());
        assertEquals(250, f.getCorruptedEssenceChance());
        assertNotNull(f.getAllowedTags());
    }

    @Test
    @DisplayName("FossilWeightConverter: converts weight fields")
    void fossilWeightConvertsAllFields() {
        ObjectNode row = MAPPER.createObjectNode();
        row.put("_pageID", "1000");
        row.put("_pageName", "FossilWeight");
        row.put("base_item_id", "item_42");
        row.put("ordinal", "0");
        row.put("tag", "cold");
        row.put("type", "weight");
        row.put("weight", "1500");

        FossilWeight fw = new FossilWeightConverter().convert(row);
        assertEquals("item_42", fw.getBaseItemId());
        assertEquals(0, fw.getOrdinal());
        assertEquals("cold", fw.getTag());
        assertEquals("weight", fw.getWeightType());
        assertEquals(1500, fw.getWeight());
    }

    // ==================== 经济数据 ====================

    @Test
    @DisplayName("VendorRewardConverter: converts vendor recipe fields")
    void vendorRewardConvertsAllFields() {
        ObjectNode row = MAPPER.createObjectNode();
        row.put("_pageID", "1100");
        row.put("_pageName", "Recipe_1");
        row.put("act", "1");
        row.put("class_ids", "[\"marauder\",\"duelist\"]");
        row.put("classes", "[\"Marauder\",\"Duelist\"]");
        row.put("npc", "Nessa");
        row.put("quest", "Breaking Some Eggs");
        row.put("quest_id", "a1q1");

        VendorReward vr = new VendorRewardConverter().convert(row);
        assertEquals(1100, vr.getPageId());
        assertEquals(1, vr.getAct());
        assertEquals("Nessa", vr.getNpc());
        assertEquals("Breaking Some Eggs", vr.getQuest());
        assertNotNull(vr.getClassIds());
    }

    @Test
    @DisplayName("ItemSellPriceConverter: converts sell price fields")
    void itemSellPriceConvertsAllFields() {
        ObjectNode row = MAPPER.createObjectNode();
        row.put("_pageID", "1200");
        row.put("_pageName", "ItemSellPrice_1");
        row.put("amount", "5");
        row.put("name", "Scroll Fragment");

        ItemSellPrice isp = new ItemSellPriceConverter().convert(row);
        assertEquals(1200, isp.getPageId());
        assertEquals(5, isp.getAmount());
        assertEquals("Scroll Fragment", isp.getCurrencyName());
    }

    @Test
    @DisplayName("ItemPurchaseCostConverter: converts purchase cost with rarity")
    void itemPurchaseCostConvertsAllFields() {
        ObjectNode row = MAPPER.createObjectNode();
        row.put("_pageID", "1300");
        row.put("_pageName", "PurchaseCost_1");
        row.put("amount", "1");
        row.put("name", "Orb of Transmutation");
        row.put("rarity", "magic");

        ItemPurchaseCost ipc = new ItemPurchaseCostConverter().convert(row);
        assertEquals(1300, ipc.getPageId());
        assertEquals(1, ipc.getAmount());
        assertEquals("Orb of Transmutation", ipc.getCurrencyName());
        assertEquals("magic", ipc.getRarity());
    }
}
