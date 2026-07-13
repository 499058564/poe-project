package com.poe.provider.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.poe.cache.model.Item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 将 Wiki Cargo {@code items} 表行转换为 {@link Item} 模型 — 完整映射全部 78 个字段。
 */
public class ItemConverter implements DataConverter<Item> {

    @Override
    public Item convert(JsonNode row) {
        Item item = new Item();

        // ── 主键与核心标识 ──
        String name = row.path("name").asText();
        item.setId(parseId(row.path("_pageID").asText()));
        if (item.getId() == 0) {
            item.setId(name.isEmpty() ? 0 : Math.abs(name.hashCode()));
        }
        item.setName(name);
        item.setNameZh(null);  // translations loaded separately
        item.setNameList(nullableText(row, "name_list"));
        item.setMetadataId(nullableText(row, "metadata_id"));
        item.setPageName(nullableText(row, "_pageName"));

        // ── 分类 ──
        item.setItemClass(row.path("class_id").asText());
        item.setClassId(row.path("class_id").asText());
        item.setFrameType(nullableText(row, "frame_type"));
        item.setRarity(nullableText(row, "rarity"));
        item.setRarityId(nullableText(row, "rarity_id"));

        // ── 关联 ──
        item.setBaseItem(nullableText(row, "base_item"));
        item.setBaseItemId(nullableText(row, "base_item_id"));
        item.setBaseItemPage(nullableText(row, "base_item_page"));

        // ── 尺寸 ──
        item.setSizeX(parseIntSafe(row, "size_x"));
        item.setSizeY(parseIntSafe(row, "size_y"));
        item.setInventoryIcon(nullableText(row, "inventory_icon"));

        // ── 属性需求 (核心值) ──
        item.setRequiredLevel(parseIntSafe(row, "required_level"));
        item.setRequiredLevelBase(parseIntSafe(row, "required_level_base"));
        item.setRequiredDexterity(parseIntSafe(row, "required_dexterity"));
        item.setRequiredIntelligence(parseIntSafe(row, "required_intelligence"));
        item.setRequiredStrength(parseIntSafe(row, "required_strength"));

        // ── 属性需求 (范围值) ──
        item.setRequiredLevelRangeAverage(parseIntSafe(row, "required_level_range_average"));
        item.setRequiredLevelRangeColour(nullableText(row, "required_level_range_colour"));
        item.setRequiredLevelRangeMaximum(parseIntSafe(row, "required_level_range_maximum"));
        item.setRequiredLevelRangeMinimum(parseIntSafe(row, "required_level_range_minimum"));
        item.setRequiredLevelRangeText(nullableText(row, "required_level_range_text"));

        item.setRequiredDexterityRangeAverage(parseIntSafe(row, "required_dexterity_range_average"));
        item.setRequiredDexterityRangeColour(nullableText(row, "required_dexterity_range_colour"));
        item.setRequiredDexterityRangeMaximum(parseIntSafe(row, "required_dexterity_range_maximum"));
        item.setRequiredDexterityRangeMinimum(parseIntSafe(row, "required_dexterity_range_minimum"));
        item.setRequiredDexterityRangeText(nullableText(row, "required_dexterity_range_text"));

        item.setRequiredIntelligenceRangeAverage(parseIntSafe(row, "required_intelligence_range_average"));
        item.setRequiredIntelligenceRangeColour(nullableText(row, "required_intelligence_range_colour"));
        item.setRequiredIntelligenceRangeMaximum(parseIntSafe(row, "required_intelligence_range_maximum"));
        item.setRequiredIntelligenceRangeMinimum(parseIntSafe(row, "required_intelligence_range_minimum"));
        item.setRequiredIntelligenceRangeText(nullableText(row, "required_intelligence_range_text"));

        item.setRequiredStrengthRangeAverage(parseIntSafe(row, "required_strength_range_average"));
        item.setRequiredStrengthRangeColour(nullableText(row, "required_strength_range_colour"));
        item.setRequiredStrengthRangeMaximum(parseIntSafe(row, "required_strength_range_maximum"));
        item.setRequiredStrengthRangeMinimum(parseIntSafe(row, "required_strength_range_minimum"));
        item.setRequiredStrengthRangeText(nullableText(row, "required_strength_range_text"));

        // ── 属性需求 (HTML) ──
        item.setRequiredLevelHtml(nullableText(row, "required_level_html"));
        item.setRequiredDexterityHtml(nullableText(row, "required_dexterity_html"));
        item.setRequiredIntelligenceHtml(nullableText(row, "required_intelligence_html"));
        item.setRequiredStrengthHtml(nullableText(row, "required_strength_html"));

        // ── 状态标记 ──
        item.setDropEnabled(parseBooleanSafe(row, "drop_enabled"));
        item.setDropLevel(parseIntSafe(row, "drop_level"));
        item.setDropLevelMaximum(parseIntSafe(row, "drop_level_maximum"));
        item.setAccountBound(parseBooleanSafe(row, "is_account_bound"));
        item.setCorrupted(parseBooleanSafe(row, "is_corrupted"));
        item.setDropRestricted(parseBooleanSafe(row, "is_drop_restricted"));
        item.setEaterOfWorldsItem(parseBooleanSafe(row, "is_eater_of_worlds_item"));
        item.setFractured(parseBooleanSafe(row, "is_fractured"));
        item.setInGame(parseBooleanSafe(row, "is_in_game"));
        item.setReplica(parseBooleanSafe(row, "is_replica"));
        item.setSearingExarchItem(parseBooleanSafe(row, "is_searing_exarch_item"));
        item.setSynthesised(parseBooleanSafe(row, "is_synthesised"));
        item.setUnmodifiable(parseBooleanSafe(row, "is_unmodifiable"));
        item.setVeiled(parseBooleanSafe(row, "is_veiled"));

        // ── 属性文本 ──
        item.setStatText(nullableText(row, "stat_text"));
        item.setExplicitStatText(nullableText(row, "explicit_stat_text"));
        item.setImplicitStatText(nullableText(row, "implicit_stat_text"));

        // ── 掉落 ──
        item.setDropText(nullableText(row, "drop_text"));
        item.setDropAreas(parseListField(row, "drop_areas"));
        item.setDropAreasHtml(nullableText(row, "drop_areas_html"));
        item.setDropMonsters(parseListField(row, "drop_monsters"));
        item.setDropRarityIds(parseListField(row, "drop_rarity_ids"));

        // ── 标签 ──
        item.setTags(parseListField(row, "tags"));
        item.setAcquisitionTags(parseListField(row, "acquisition_tags"));
        item.setInfluences(parseListField(row, "influences"));

        // ── 描述与提示 ──
        item.setDescription(nullableText(row, "description"));
        item.setFlavourText(nullableText(row, "flavour_text"));
        item.setHelpText(nullableText(row, "help_text"));

        // ── HTML ──
        item.setHtml(nullableText(row, "html"));
        item.setInfoboxHtml(nullableText(row, "infobox_html"));
        item.setMetaboxHtml(nullableText(row, "metabox_html"));

        // ── 图标 ──
        item.setAlternateArtInventoryIcons(parseListField(row, "alternate_art_inventory_icons"));

        // ── 品质 ──
        item.setQuality(parseIntSafe(row, "quality"));

        // ── 版本 ──
        item.setReleaseVersion(nullableText(row, "release_version"));
        item.setRemovalVersion(nullableText(row, "removal_version"));

        // ── Wiki ──
        item.setWikiUrl("https://www.poewiki.net/wiki/" + escapeWikiPath(name));
        item.setVersion("");

        return item;
    }

    // ── Cargo 列表字段 → List<String> ──
    // Cargo 中的列表字段通常以逗号分隔
    static List<String> parseListField(JsonNode node, String field) {
        String text = node.path(field).asText();
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(text.split("\\s*,\\s*")));
    }

    // ── 公开辅助方法 ──

    static int parseId(String pageId) {
        try {
            if (pageId != null && !pageId.isEmpty()) {
                return Integer.parseInt(pageId);
            }
        } catch (NumberFormatException ignored) { }
        return 0;
    }

    static int parseIntSafe(JsonNode node, String field) {
        String text = node.path(field).asText();
        if (text.isEmpty()) return 0;
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    static boolean parseBooleanSafe(JsonNode node, String field) {
        String text = node.path(field).asText();
        return "1".equals(text) || "true".equalsIgnoreCase(text);
    }

    static double parseDoubleSafe(JsonNode node, String field) {
        String text = node.path(field).asText();
        if (text.isEmpty()) return 0.0;
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    static String nullableText(JsonNode node, String field) {
        String text = node.path(field).asText();
        return (text == null || text.isEmpty()) ? null : text;
    }

    static String nullToNull(String s) {
        return (s == null || s.isEmpty()) ? null : s;
    }

    static String escapeWikiPath(String pageName) {
        if (pageName == null || pageName.isEmpty()) return "";
        return pageName.replace(" ", "_");
    }

    /** 将 JSON 节点序列化为字符串，缺失或 null 节点返回 null */
    static String toJsonOrNull(JsonNode node) {
        if (node.isMissingNode() || node.isNull()) return null;
        try {
            return node.toString();
        } catch (Exception e) {
            return null;
        }
    }
}
