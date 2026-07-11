package com.poe.provider.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.poe.cache.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EquipmentSubtableConverterTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // ==================== WeaponConverter ====================

    @Test
    @DisplayName("WeaponConverter: converts all damage fields")
    void weaponConverterConvertsAllFields() {
        ObjectNode row = MAPPER.createObjectNode();
        row.put("_pageID", "12345");
        row.put("_pageName", "Rusted_Sword");
        row.put("attack_speed", "1.45");
        row.put("critical_strike_chance", "5.0");
        row.put("weapon_range", "11");
        row.put("physical_damage_min", "5");
        row.put("physical_damage_max", "12");
        row.put("fire_damage_min", "8");
        row.put("fire_damage_max", "15");
        row.put("cold_damage_min", "0");
        row.put("cold_damage_max", "0");
        row.put("lightning_damage_min", "1");
        row.put("lightning_damage_max", "40");
        row.put("chaos_damage_min", "0");
        row.put("chaos_damage_max", "0");

        Weapon w = new WeaponConverter().convert(row);

        assertEquals(12345, w.getPageId());
        assertEquals("Rusted_Sword", w.getPageName());
        assertEquals(1.45, w.getAttackSpeed(), 0.001);
        assertEquals(5.0, w.getCriticalStrikeChance(), 0.001);
        assertEquals(5, w.getPhysicalDamageMin());
        assertEquals(12, w.getPhysicalDamageMax());
        assertEquals(8, w.getFireDamageMin());
        assertEquals(1, w.getLightningDamageMin());
        assertEquals(40, w.getLightningDamageMax());
    }

    @Test
    @DisplayName("WeaponConverter: handles missing fields with defaults")
    void weaponConverterHandlesMissingFields() {
        ObjectNode row = MAPPER.createObjectNode();
        row.put("_pageID", "1");
        row.put("_pageName", "Test");

        Weapon w = new WeaponConverter().convert(row);
        assertEquals(1, w.getPageId());
        assertEquals(0.0, w.getAttackSpeed());
        assertEquals(0, w.getPhysicalDamageMin());
    }

    // ==================== ArmourConverter ====================

    @Test
    @DisplayName("ArmourConverter: converts all defence fields")
    void armourConverterConvertsAllFields() {
        ObjectNode row = MAPPER.createObjectNode();
        row.put("_pageID", "100");
        row.put("_pageName", "Simple_Robe");
        row.put("armour_min", "10");
        row.put("armour_max", "15");
        row.put("evasion_min", "0");
        row.put("evasion_max", "0");
        row.put("energy_shield_min", "20");
        row.put("energy_shield_max", "25");
        row.put("ward_min", "0");
        row.put("ward_max", "0");
        row.put("movement_speed", "-3");

        Armour a = new ArmourConverter().convert(row);
        assertEquals(100, a.getPageId());
        assertEquals(10, a.getArmourMin());
        assertEquals(15, a.getArmourMax());
        assertEquals(20, a.getEnergyShieldMin());
        assertEquals(25, a.getEnergyShieldMax());
        assertEquals(-3, a.getMovementSpeed());
    }

    // ==================== ShieldConverter ====================

    @Test
    @DisplayName("ShieldConverter: converts block value")
    void shieldConverterConvertsBlock() {
        ObjectNode row = MAPPER.createObjectNode();
        row.put("_pageID", "200");
        row.put("_pageName", "Buckler");
        row.put("block", "25");

        Shield s = new ShieldConverter().convert(row);
        assertEquals(200, s.getPageId());
        assertEquals(25, s.getBlock());
    }

    // ==================== AmuletConverter ====================

    @Test
    @DisplayName("AmuletConverter: converts talisman fields")
    void amuletConverterConvertsTalisman() {
        ObjectNode row = MAPPER.createObjectNode();
        row.put("_pageID", "300");
        row.put("_pageName", "Wereclaw_Talisman");
        row.put("is_talisman", "1");
        row.put("talisman_tier", "2");

        Amulet a = new AmuletConverter().convert(row);
        assertTrue(a.isTalisman());
        assertEquals(2, a.getTalismanTier());
    }

    @Test
    @DisplayName("AmuletConverter: non-talisman amulet")
    void amuletConverterNonTalisman() {
        ObjectNode row = MAPPER.createObjectNode();
        row.put("_pageID", "301");
        row.put("_pageName", "Paua_Amulet");
        row.put("is_talisman", "0");
        row.put("talisman_tier", "0");

        Amulet a = new AmuletConverter().convert(row);
        assertFalse(a.isTalisman());
        assertEquals(0, a.getTalismanTier());
    }

    // ==================== FlaskConverter ====================

    @Test
    @DisplayName("FlaskConverter: converts utility flask")
    void flaskConverterConvertsUtilityFlask() {
        ObjectNode row = MAPPER.createObjectNode();
        row.put("_pageID", "400");
        row.put("_pageName", "Amethyst_Flask");
        row.put("charges_max", "65");
        row.put("charges_per_use", "35");
        row.put("duration", "6.5");
        row.put("life", "0");
        row.put("mana", "0");

        Flask f = new FlaskConverter().convert(row);
        assertEquals(65, f.getChargesMax());
        assertEquals(35, f.getChargesPerUse());
        assertEquals(6.5, f.getDuration(), 0.001);
        assertEquals(0, f.getLife());
        assertEquals(0, f.getMana());
    }

    @Test
    @DisplayName("FlaskConverter: converts life flask")
    void flaskConverterConvertsLifeFlask() {
        ObjectNode row = MAPPER.createObjectNode();
        row.put("_pageID", "401");
        row.put("_pageName", "Divine_Life_Flask");
        row.put("charges_max", "30");
        row.put("charges_per_use", "10");
        row.put("duration", "0");
        row.put("life", "100");
        row.put("mana", "0");

        Flask f = new FlaskConverter().convert(row);
        assertEquals(100, f.getLife());
        assertEquals(0, f.getMana());
        assertEquals(0.0, f.getDuration());
    }

    // ==================== JewelConverter ====================

    @Test
    @DisplayName("JewelConverter: converts jewel fields")
    void jewelConverterConvertsFields() {
        ObjectNode row = MAPPER.createObjectNode();
        row.put("_pageID", "500");
        row.put("_pageName", "Cobalt_Jewel");
        row.put("jewel_limit", "1");
        row.put("radius_html", "<span>Large</span>");

        Jewel j = new JewelConverter().convert(row);
        assertEquals("1", j.getJewelLimit());
        assertEquals("<span>Large</span>", j.getRadiusHtml());
    }

    @Test
    @DisplayName("JewelConverter: handles null fields")
    void jewelConverterHandlesNullFields() {
        ObjectNode row = MAPPER.createObjectNode();
        row.put("_pageID", "501");
        row.put("_pageName", "Test_Jewel");

        Jewel j = new JewelConverter().convert(row);
        assertNull(j.getJewelLimit());
        assertNull(j.getRadiusHtml());
    }

    // ==================== StackableConverter ====================

    @Test
    @DisplayName("StackableConverter: converts stack size fields")
    void stackableConverterConvertsFields() {
        ObjectNode row = MAPPER.createObjectNode();
        row.put("_pageID", "600");
        row.put("_pageName", "Chaos_Orb");
        row.put("stack_size", "10");
        row.put("stack_size_currency_tab", "5000");

        Stackable s = new StackableConverter().convert(row);
        assertEquals(10, s.getStackSize());
        assertEquals(5000, s.getStackSizeCurrencyTab());
    }

    // ==================== MapConverter ====================

    @Test
    @DisplayName("MapConverter: converts all map fields")
    void mapConverterConvertsAllFields() {
        ObjectNode row = MAPPER.createObjectNode();
        row.put("_pageID", "700");
        row.put("_pageName", "Beach_Map");
        row.put("area_id", "1_1_1");
        row.put("area_level", "1");
        row.put("guild_character", "A");
        row.put("series", "Original");
        row.put("tier", "1");
        row.put("unique_area_id", "U1_1_1");
        row.put("unique_area_level", "68");
        row.put("unique_guild_character", "B");

        GameMap m = new MapConverter().convert(row);
        assertEquals(700, m.getPageId());
        assertEquals("Beach_Map", m.getPageName());
        assertEquals("1_1_1", m.getAreaId());
        assertEquals(1, m.getAreaLevel());
        assertEquals("A", m.getGuildCharacter());
        assertEquals("Original", m.getSeries());
        assertEquals(1, m.getTier());
        assertEquals("U1_1_1", m.getUniqueAreaId());
        assertEquals(68, m.getUniqueAreaLevel());
    }

    // ==================== MapFragmentConverter ====================

    @Test
    @DisplayName("MapFragmentConverter: converts fragment limit")
    void mapFragmentConverterConvertsLimit() {
        ObjectNode row = MAPPER.createObjectNode();
        row.put("_pageID", "800");
        row.put("_pageName", "Sacrifice_at_Dusk");
        row.put("map_fragment_limit", "1");

        MapFragment mf = new MapFragmentConverter().convert(row);
        assertEquals(800, mf.getPageId());
        assertEquals(1, mf.getMapFragmentLimit());
    }

    // ==================== MapSeriesConverter ====================

    @Test
    @DisplayName("MapSeriesConverter: converts series fields")
    void mapSeriesConverterConvertsFields() {
        ObjectNode row = MAPPER.createObjectNode();
        row.put("_pageID", "900");
        row.put("_pageName", "Atlas_of_Worlds");
        row.put("id", "original");
        row.put("name", "Original");
        row.put("ordinal", "1");

        MapSeries ms = new MapSeriesConverter().convert(row);
        assertEquals("original", ms.getSeriesId());
        assertEquals("Original", ms.getName());
        assertEquals(1, ms.getOrdinal());
    }

    // ==================== DivinationCardConverter ====================

    @Test
    @DisplayName("DivinationCardConverter: converts card fields")
    void divinationCardConverterConvertsFields() {
        ObjectNode row = MAPPER.createObjectNode();
        row.put("_pageID", "1000");
        row.put("_pageName", "The_Doctor");
        row.put("card_art", "The_Doctor_card_art.png");
        row.put("card_background", "1,2,3");

        DivinationCard dc = new DivinationCardConverter().convert(row);
        assertEquals("The_Doctor_card_art.png", dc.getCardArt());
        assertEquals("1,2,3", dc.getCardBackground());
    }

    @Test
    @DisplayName("DivinationCardConverter: handles empty fields")
    void divinationCardConverterHandlesEmptyFields() {
        ObjectNode row = MAPPER.createObjectNode();
        row.put("_pageID", "1001");
        row.put("_pageName", "Test_Card");

        DivinationCard dc = new DivinationCardConverter().convert(row);
        assertNull(dc.getCardArt());
        assertNull(dc.getCardBackground());
    }
}
