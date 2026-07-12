package com.poe.cache.dao;

import javax.sql.DataSource;
import com.poe.cache.model.Area;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * areas 表数据访问对象。
 * <p>
 * 表使用 page_id 作为主键，记录 PoE 各区域（城镇/地图/试炼等）的完整信息。
 */
public class AreaDao implements CrudRepository<Area, Integer> {

    private final DataSource dataSource;

    public AreaDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 插入单条区域记录。
     *
     * @param entity 区域实体
     */
    @Override
    public void insert(Area entity) {
        String sql = "INSERT INTO areas (page_id, page_name, act, area_level, area_type_tags, "
            + "boss_monster_ids, connection_ids, entry_npc, entry_text, flavour_text, "
            + "has_waypoint, id, infobox_html, is_hideout_area, is_labyrinth_airlock_area, "
            + "is_labyrinth_area, is_labyrinth_boss_area, is_legacy_map_area, is_map_area, "
            + "is_town_area, is_unique_map_area, is_vaal_area, level_restriction_max, "
            + "loading_screen, main_page, mainpage_categories, modifier_ids, monster_ids, "
            + "name, parent_area_id, release_version, removal_version, screenshot, "
            + "stat_text, strongbox_max_count, strongbox_spawn_chance, strongbox_weight_magic, "
            + "strongbox_weight_normal, strongbox_weight_rare, strongbox_weight_unique, "
            + "tags, vaal_area_ids, vaal_area_spawn_chance) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert area: " + entity.getPageId(), e);
        }
    }

    /**
     * 批量插入区域记录（事务）。
     *
     * @param entities 区域实体列表（非空）
     */
    @Override
    public void batchInsert(List<Area> entities) {
        String sql = "INSERT INTO areas (page_id, page_name, act, area_level, area_type_tags, "
            + "boss_monster_ids, connection_ids, entry_npc, entry_text, flavour_text, "
            + "has_waypoint, id, infobox_html, is_hideout_area, is_labyrinth_airlock_area, "
            + "is_labyrinth_area, is_labyrinth_boss_area, is_legacy_map_area, is_map_area, "
            + "is_town_area, is_unique_map_area, is_vaal_area, level_restriction_max, "
            + "loading_screen, main_page, mainpage_categories, modifier_ids, monster_ids, "
            + "name, parent_area_id, release_version, removal_version, screenshot, "
            + "stat_text, strongbox_max_count, strongbox_spawn_chance, strongbox_weight_magic, "
            + "strongbox_weight_normal, strongbox_weight_rare, strongbox_weight_unique, "
            + "tags, vaal_area_ids, vaal_area_spawn_chance) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Area entity : entities) {
                    setParams(ps, entity);
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
            throw new RuntimeException("Failed to batch insert areas", e);
        }
    }

    /**
     * 根据主键查询区域。
     *
     * @param pageId Wiki 页面 ID
     * @return 区域实体（可能为空）
     */
    @Override
    public Optional<Area> findById(Integer pageId) {
        String sql = "SELECT * FROM areas WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find area by id: " + pageId, e);
        }
        return Optional.empty();
    }

    /**
     * 查询全部区域记录。
     *
     * @return 按 page_id 升序排列的区域列表
     */
    @Override
    public List<Area> findAll() {
        List<Area> list = new ArrayList<>();
        String sql = "SELECT * FROM areas ORDER BY page_id";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all areas", e);
        }
        return list;
    }

    /**
     * 根据主键删除区域。
     *
     * @param pageId Wiki 页面 ID
     */
    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM areas WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete area: " + pageId, e);
        }
    }

    /**
     * 统计区域记录总数。
     *
     * @return areas 表行数
     */
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM areas";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count areas", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, Area a) throws SQLException {
        int idx = 1;
        ps.setInt(idx++, a.getPageId());
        ps.setString(idx++, a.getPageName());
        ps.setInt(idx++, a.getAct());
        ps.setInt(idx++, a.getAreaLevel());
        ps.setString(idx++, a.getAreaTypeTags());
        ps.setString(idx++, a.getBossMonsterIds());
        ps.setString(idx++, a.getConnectionIds());
        ps.setString(idx++, a.getEntryNpc());
        ps.setString(idx++, a.getEntryText());
        ps.setString(idx++, a.getFlavourText());
        ps.setInt(idx++, a.getHasWaypoint());
        ps.setString(idx++, a.getId());
        ps.setString(idx++, a.getInfoboxHtml());
        ps.setInt(idx++, a.getIsHideoutArea());
        ps.setInt(idx++, a.getIsLabyrinthAirlockArea());
        ps.setInt(idx++, a.getIsLabyrinthArea());
        ps.setInt(idx++, a.getIsLabyrinthBossArea());
        ps.setInt(idx++, a.getIsLegacyMapArea());
        ps.setInt(idx++, a.getIsMapArea());
        ps.setInt(idx++, a.getIsTownArea());
        ps.setInt(idx++, a.getIsUniqueMapArea());
        ps.setInt(idx++, a.getIsVaalArea());
        ps.setInt(idx++, a.getLevelRestrictionMax());
        ps.setString(idx++, a.getLoadingScreen());
        ps.setString(idx++, a.getMainPage());
        ps.setString(idx++, a.getMainpageCategories());
        ps.setString(idx++, a.getModifierIds());
        ps.setString(idx++, a.getMonsterIds());
        ps.setString(idx++, a.getName());
        ps.setString(idx++, a.getParentAreaId());
        ps.setString(idx++, a.getReleaseVersion());
        ps.setString(idx++, a.getRemovalVersion());
        ps.setString(idx++, a.getScreenshot());
        ps.setString(idx++, a.getStatText());
        ps.setInt(idx++, a.getStrongboxMaxCount());
        ps.setInt(idx++, a.getStrongboxSpawnChance());
        ps.setInt(idx++, a.getStrongboxWeightMagic());
        ps.setInt(idx++, a.getStrongboxWeightNormal());
        ps.setInt(idx++, a.getStrongboxWeightRare());
        ps.setInt(idx++, a.getStrongboxWeightUnique());
        ps.setString(idx++, a.getTags());
        ps.setString(idx++, a.getVaalAreaIds());
        ps.setInt(idx++, a.getVaalAreaSpawnChance());
    }

    private Area mapRow(ResultSet rs) throws SQLException {
        Area a = new Area();
        a.setPageId(rs.getInt("page_id"));
        a.setPageName(rs.getString("page_name"));
        a.setAct(rs.getInt("act"));
        a.setAreaLevel(rs.getInt("area_level"));
        a.setAreaTypeTags(rs.getString("area_type_tags"));
        a.setBossMonsterIds(rs.getString("boss_monster_ids"));
        a.setConnectionIds(rs.getString("connection_ids"));
        a.setEntryNpc(rs.getString("entry_npc"));
        a.setEntryText(rs.getString("entry_text"));
        a.setFlavourText(rs.getString("flavour_text"));
        a.setHasWaypoint(rs.getInt("has_waypoint"));
        a.setId(rs.getString("id"));
        a.setInfoboxHtml(rs.getString("infobox_html"));
        a.setIsHideoutArea(rs.getInt("is_hideout_area"));
        a.setIsLabyrinthAirlockArea(rs.getInt("is_labyrinth_airlock_area"));
        a.setIsLabyrinthArea(rs.getInt("is_labyrinth_area"));
        a.setIsLabyrinthBossArea(rs.getInt("is_labyrinth_boss_area"));
        a.setIsLegacyMapArea(rs.getInt("is_legacy_map_area"));
        a.setIsMapArea(rs.getInt("is_map_area"));
        a.setIsTownArea(rs.getInt("is_town_area"));
        a.setIsUniqueMapArea(rs.getInt("is_unique_map_area"));
        a.setIsVaalArea(rs.getInt("is_vaal_area"));
        a.setLevelRestrictionMax(rs.getInt("level_restriction_max"));
        a.setLoadingScreen(rs.getString("loading_screen"));
        a.setMainPage(rs.getString("main_page"));
        a.setMainpageCategories(rs.getString("mainpage_categories"));
        a.setModifierIds(rs.getString("modifier_ids"));
        a.setMonsterIds(rs.getString("monster_ids"));
        a.setName(rs.getString("name"));
        a.setParentAreaId(rs.getString("parent_area_id"));
        a.setReleaseVersion(rs.getString("release_version"));
        a.setRemovalVersion(rs.getString("removal_version"));
        a.setScreenshot(rs.getString("screenshot"));
        a.setStatText(rs.getString("stat_text"));
        a.setStrongboxMaxCount(rs.getInt("strongbox_max_count"));
        a.setStrongboxSpawnChance(rs.getInt("strongbox_spawn_chance"));
        a.setStrongboxWeightMagic(rs.getInt("strongbox_weight_magic"));
        a.setStrongboxWeightNormal(rs.getInt("strongbox_weight_normal"));
        a.setStrongboxWeightRare(rs.getInt("strongbox_weight_rare"));
        a.setStrongboxWeightUnique(rs.getInt("strongbox_weight_unique"));
        a.setTags(rs.getString("tags"));
        a.setVaalAreaIds(rs.getString("vaal_area_ids"));
        a.setVaalAreaSpawnChance(rs.getInt("vaal_area_spawn_chance"));
        return a;
    }
}
