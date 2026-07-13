package com.poe.cache.dao;

import javax.sql.DataSource;
import com.poe.cache.model.Item;
import com.poe.cache.util.JsonUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * base_items 表数据访问对象 — 完整映射 Wiki items Cargo 全部 78 个字段。
 */
public class ItemDao implements CrudRepository<Item, Integer> {

    private static final String ALL_COLUMNS =
        "id, name, name_zh, name_list, metadata_id, _page_name, " +
        "class, class_id, frame_type, rarity, rarity_id, " +
        "base_item, base_item_id, base_item_page, " +
        "size_x, size_y, inventory_icon, " +
        "required_level, required_level_base, " +
        "required_dexterity, required_intelligence, required_strength, " +
        "required_level_range_average, required_level_range_colour, required_level_range_maximum, required_level_range_minimum, required_level_range_text, " +
        "required_dexterity_range_average, required_dexterity_range_colour, required_dexterity_range_maximum, required_dexterity_range_minimum, required_dexterity_range_text, " +
        "required_intelligence_range_average, required_intelligence_range_colour, required_intelligence_range_maximum, required_intelligence_range_minimum, required_intelligence_range_text, " +
        "required_strength_range_average, required_strength_range_colour, required_strength_range_maximum, required_strength_range_minimum, required_strength_range_text, " +
        "required_level_html, required_dexterity_html, required_intelligence_html, required_strength_html, " +
        "drop_enabled, drop_level, drop_level_maximum, " +
        "is_account_bound, is_corrupted, is_drop_restricted, is_eater_of_worlds_item, is_fractured, is_in_game, is_replica, is_searing_exarch_item, is_synthesised, is_unmodifiable, is_veiled, " +
        "stat_text, explicit_stat_text, implicit_stat_text, " +
        "drop_text, drop_areas, drop_areas_html, drop_monsters, drop_rarity_ids, " +
        "tags, acquisition_tags, influences, " +
        "description, flavour_text, help_text, " +
        "html, infobox_html, metabox_html, " +
        "alternate_art_inventory_icons, " +
        "quality, release_version, removal_version, wiki_url, version";

    private static final int COLUMN_COUNT = ALL_COLUMNS.split(",").length;

    private static final String INSERT_SQL =
        "INSERT INTO base_items (" + ALL_COLUMNS + ") VALUES (" +
        String.join(",", java.util.Collections.nCopies(COLUMN_COUNT, "?")) + ")";

    private final DataSource dataSource;

    public ItemDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insert(Item item) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {
            setItemParams(ps, item);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert item: " + item.getId(), e);
        }
    }

    @Override
    public void batchInsert(List<Item> items) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {
                for (Item item : items) {
                    setItemParams(ps, item);
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to batch insert items", e);
        }
    }

    @Override
    public Optional<Item> findById(Integer id) {
        String sql = "SELECT " + ALL_COLUMNS + " FROM base_items WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find item by id: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Item> findAll() {
        String sql = "SELECT " + ALL_COLUMNS + " FROM base_items ORDER BY id";
        List<Item> items = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                items.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all items", e);
        }
        return items;
    }

    @Override
    public void deleteById(Integer id) {
        String sql = "DELETE FROM base_items WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete item by id: " + id, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM base_items";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count items", e);
        }
        return 0;
    }

    private void setItemParams(PreparedStatement ps, Item item) throws SQLException {
        int idx = 1;
        ps.setInt(idx++, item.getId());
        ps.setString(idx++, item.getName());
        ps.setString(idx++, item.getNameZh());
        ps.setString(idx++, item.getNameList());
        ps.setString(idx++, item.getMetadataId());
        ps.setString(idx++, item.getPageName());

        ps.setString(idx++, item.getItemClass());
        ps.setString(idx++, item.getClassId());
        ps.setString(idx++, item.getFrameType());
        ps.setString(idx++, item.getRarity());
        ps.setString(idx++, item.getRarityId());

        ps.setString(idx++, item.getBaseItem());
        ps.setString(idx++, item.getBaseItemId());
        ps.setString(idx++, item.getBaseItemPage());

        ps.setInt(idx++, item.getSizeX());
        ps.setInt(idx++, item.getSizeY());
        ps.setString(idx++, item.getInventoryIcon());

        ps.setInt(idx++, item.getRequiredLevel());
        ps.setInt(idx++, item.getRequiredLevelBase());
        ps.setInt(idx++, item.getRequiredDexterity());
        ps.setInt(idx++, item.getRequiredIntelligence());
        ps.setInt(idx++, item.getRequiredStrength());

        ps.setInt(idx++, item.getRequiredLevelRangeAverage());
        ps.setString(idx++, item.getRequiredLevelRangeColour());
        ps.setInt(idx++, item.getRequiredLevelRangeMaximum());
        ps.setInt(idx++, item.getRequiredLevelRangeMinimum());
        ps.setString(idx++, item.getRequiredLevelRangeText());

        ps.setInt(idx++, item.getRequiredDexterityRangeAverage());
        ps.setString(idx++, item.getRequiredDexterityRangeColour());
        ps.setInt(idx++, item.getRequiredDexterityRangeMaximum());
        ps.setInt(idx++, item.getRequiredDexterityRangeMinimum());
        ps.setString(idx++, item.getRequiredDexterityRangeText());

        ps.setInt(idx++, item.getRequiredIntelligenceRangeAverage());
        ps.setString(idx++, item.getRequiredIntelligenceRangeColour());
        ps.setInt(idx++, item.getRequiredIntelligenceRangeMaximum());
        ps.setInt(idx++, item.getRequiredIntelligenceRangeMinimum());
        ps.setString(idx++, item.getRequiredIntelligenceRangeText());

        ps.setInt(idx++, item.getRequiredStrengthRangeAverage());
        ps.setString(idx++, item.getRequiredStrengthRangeColour());
        ps.setInt(idx++, item.getRequiredStrengthRangeMaximum());
        ps.setInt(idx++, item.getRequiredStrengthRangeMinimum());
        ps.setString(idx++, item.getRequiredStrengthRangeText());

        ps.setString(idx++, item.getRequiredLevelHtml());
        ps.setString(idx++, item.getRequiredDexterityHtml());
        ps.setString(idx++, item.getRequiredIntelligenceHtml());
        ps.setString(idx++, item.getRequiredStrengthHtml());

        ps.setBoolean(idx++, item.isDropEnabled());
        ps.setInt(idx++, item.getDropLevel());
        ps.setInt(idx++, item.getDropLevelMaximum());

        ps.setBoolean(idx++, item.isAccountBound());
        ps.setBoolean(idx++, item.isCorrupted());
        ps.setBoolean(idx++, item.isDropRestricted());
        ps.setBoolean(idx++, item.isEaterOfWorldsItem());
        ps.setBoolean(idx++, item.isFractured());
        ps.setBoolean(idx++, item.isInGame());
        ps.setBoolean(idx++, item.isReplica());
        ps.setBoolean(idx++, item.isSearingExarchItem());
        ps.setBoolean(idx++, item.isSynthesised());
        ps.setBoolean(idx++, item.isUnmodifiable());
        ps.setBoolean(idx++, item.isVeiled());

        ps.setString(idx++, item.getStatText());
        ps.setString(idx++, item.getExplicitStatText());
        ps.setString(idx++, item.getImplicitStatText());

        ps.setString(idx++, item.getDropText());
        ps.setString(idx++, JsonUtil.toJson(item.getDropAreas()));
        ps.setString(idx++, item.getDropAreasHtml());
        ps.setString(idx++, JsonUtil.toJson(item.getDropMonsters()));
        ps.setString(idx++, JsonUtil.toJson(item.getDropRarityIds()));

        ps.setString(idx++, JsonUtil.toJson(item.getTags()));
        ps.setString(idx++, JsonUtil.toJson(item.getAcquisitionTags()));
        ps.setString(idx++, JsonUtil.toJson(item.getInfluences()));

        ps.setString(idx++, item.getDescription());
        ps.setString(idx++, item.getFlavourText());
        ps.setString(idx++, item.getHelpText());

        ps.setString(idx++, item.getHtml());
        ps.setString(idx++, item.getInfoboxHtml());
        ps.setString(idx++, item.getMetaboxHtml());

        ps.setString(idx++, JsonUtil.toJson(item.getAlternateArtInventoryIcons()));

        ps.setInt(idx++, item.getQuality());
        ps.setString(idx++, item.getReleaseVersion());
        ps.setString(idx++, item.getRemovalVersion());
        ps.setString(idx++, item.getWikiUrl());
        ps.setString(idx++, item.getVersion());
    }

    private Item mapRow(ResultSet rs) throws SQLException {
        Item item = new Item();
        item.setId(rs.getInt("id"));
        item.setName(rs.getString("name"));
        item.setNameZh(rs.getString("name_zh"));
        item.setNameList(rs.getString("name_list"));
        item.setMetadataId(rs.getString("metadata_id"));
        item.setPageName(rs.getString("_page_name"));

        item.setItemClass(rs.getString("class"));
        item.setClassId(rs.getString("class_id"));
        item.setFrameType(rs.getString("frame_type"));
        item.setRarity(rs.getString("rarity"));
        item.setRarityId(rs.getString("rarity_id"));

        item.setBaseItem(rs.getString("base_item"));
        item.setBaseItemId(rs.getString("base_item_id"));
        item.setBaseItemPage(rs.getString("base_item_page"));

        item.setSizeX(rs.getInt("size_x"));
        item.setSizeY(rs.getInt("size_y"));
        item.setInventoryIcon(rs.getString("inventory_icon"));

        item.setRequiredLevel(rs.getInt("required_level"));
        item.setRequiredLevelBase(rs.getInt("required_level_base"));
        item.setRequiredDexterity(rs.getInt("required_dexterity"));
        item.setRequiredIntelligence(rs.getInt("required_intelligence"));
        item.setRequiredStrength(rs.getInt("required_strength"));

        item.setRequiredLevelRangeAverage(rs.getInt("required_level_range_average"));
        item.setRequiredLevelRangeColour(rs.getString("required_level_range_colour"));
        item.setRequiredLevelRangeMaximum(rs.getInt("required_level_range_maximum"));
        item.setRequiredLevelRangeMinimum(rs.getInt("required_level_range_minimum"));
        item.setRequiredLevelRangeText(rs.getString("required_level_range_text"));

        item.setRequiredDexterityRangeAverage(rs.getInt("required_dexterity_range_average"));
        item.setRequiredDexterityRangeColour(rs.getString("required_dexterity_range_colour"));
        item.setRequiredDexterityRangeMaximum(rs.getInt("required_dexterity_range_maximum"));
        item.setRequiredDexterityRangeMinimum(rs.getInt("required_dexterity_range_minimum"));
        item.setRequiredDexterityRangeText(rs.getString("required_dexterity_range_text"));

        item.setRequiredIntelligenceRangeAverage(rs.getInt("required_intelligence_range_average"));
        item.setRequiredIntelligenceRangeColour(rs.getString("required_intelligence_range_colour"));
        item.setRequiredIntelligenceRangeMaximum(rs.getInt("required_intelligence_range_maximum"));
        item.setRequiredIntelligenceRangeMinimum(rs.getInt("required_intelligence_range_minimum"));
        item.setRequiredIntelligenceRangeText(rs.getString("required_intelligence_range_text"));

        item.setRequiredStrengthRangeAverage(rs.getInt("required_strength_range_average"));
        item.setRequiredStrengthRangeColour(rs.getString("required_strength_range_colour"));
        item.setRequiredStrengthRangeMaximum(rs.getInt("required_strength_range_maximum"));
        item.setRequiredStrengthRangeMinimum(rs.getInt("required_strength_range_minimum"));
        item.setRequiredStrengthRangeText(rs.getString("required_strength_range_text"));

        item.setRequiredLevelHtml(rs.getString("required_level_html"));
        item.setRequiredDexterityHtml(rs.getString("required_dexterity_html"));
        item.setRequiredIntelligenceHtml(rs.getString("required_intelligence_html"));
        item.setRequiredStrengthHtml(rs.getString("required_strength_html"));

        item.setDropEnabled(rs.getBoolean("drop_enabled"));
        item.setDropLevel(rs.getInt("drop_level"));
        item.setDropLevelMaximum(rs.getInt("drop_level_maximum"));

        item.setAccountBound(rs.getBoolean("is_account_bound"));
        item.setCorrupted(rs.getBoolean("is_corrupted"));
        item.setDropRestricted(rs.getBoolean("is_drop_restricted"));
        item.setEaterOfWorldsItem(rs.getBoolean("is_eater_of_worlds_item"));
        item.setFractured(rs.getBoolean("is_fractured"));
        item.setInGame(rs.getBoolean("is_in_game"));
        item.setReplica(rs.getBoolean("is_replica"));
        item.setSearingExarchItem(rs.getBoolean("is_searing_exarch_item"));
        item.setSynthesised(rs.getBoolean("is_synthesised"));
        item.setUnmodifiable(rs.getBoolean("is_unmodifiable"));
        item.setVeiled(rs.getBoolean("is_veiled"));

        item.setStatText(rs.getString("stat_text"));
        item.setExplicitStatText(rs.getString("explicit_stat_text"));
        item.setImplicitStatText(rs.getString("implicit_stat_text"));

        item.setDropText(rs.getString("drop_text"));
        item.setDropAreas(JsonUtil.fromJson(rs.getString("drop_areas")));
        item.setDropAreasHtml(rs.getString("drop_areas_html"));
        item.setDropMonsters(JsonUtil.fromJson(rs.getString("drop_monsters")));
        item.setDropRarityIds(JsonUtil.fromJson(rs.getString("drop_rarity_ids")));

        item.setTags(JsonUtil.fromJson(rs.getString("tags")));
        item.setAcquisitionTags(JsonUtil.fromJson(rs.getString("acquisition_tags")));
        item.setInfluences(JsonUtil.fromJson(rs.getString("influences")));

        item.setDescription(rs.getString("description"));
        item.setFlavourText(rs.getString("flavour_text"));
        item.setHelpText(rs.getString("help_text"));

        item.setHtml(rs.getString("html"));
        item.setInfoboxHtml(rs.getString("infobox_html"));
        item.setMetaboxHtml(rs.getString("metabox_html"));

        item.setAlternateArtInventoryIcons(JsonUtil.fromJson(rs.getString("alternate_art_inventory_icons")));

        item.setQuality(rs.getInt("quality"));
        item.setReleaseVersion(rs.getString("release_version"));
        item.setRemovalVersion(rs.getString("removal_version"));
        item.setWikiUrl(rs.getString("wiki_url"));
        item.setVersion(rs.getString("version"));

        return item;
    }
}
