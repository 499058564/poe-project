package com.poe.cache.dao;

import com.poe.cache.model.IncursionRooms;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class IncursionRoomsDao implements CrudRepository<IncursionRooms, Integer> {

    private final javax.sql.DataSource dataSource;

    public IncursionRoomsDao(javax.sql.DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insert(IncursionRooms entity) {
        String sql = "INSERT INTO incursion_rooms (page_id, page_name, architect_metadata_id, architect_name, "
            + "description, flavour_text, icon, room_id, min_level, modifier_ids, name, stat_text, "
            + "tier, upgrade_room_id) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert incursion_rooms: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<IncursionRooms> entities) {
        String sql = "INSERT INTO incursion_rooms (page_id, page_name, architect_metadata_id, architect_name, "
            + "description, flavour_text, icon, room_id, min_level, modifier_ids, name, stat_text, "
            + "tier, upgrade_room_id) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (IncursionRooms entity : entities) {
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
            throw new RuntimeException("Failed to batch insert incursion_rooms", e);
        }
    }

    @Override
    public Optional<IncursionRooms> findById(Integer pageId) {
        String sql = "SELECT * FROM incursion_rooms WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find incursion_rooms by id: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<IncursionRooms> findAll() {
        List<IncursionRooms> list = new ArrayList<>();
        String sql = "SELECT * FROM incursion_rooms ORDER BY page_id";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all incursion_rooms", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM incursion_rooms WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete incursion_rooms: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM incursion_rooms";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count incursion_rooms", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, IncursionRooms i) throws SQLException {
        ps.setInt(1, i.getPageId());
        ps.setString(2, i.getPageName());
        ps.setString(3, i.getArchitectMetadataId());
        ps.setString(4, i.getArchitectName());
        ps.setString(5, i.getDescription());
        ps.setString(6, i.getFlavourText());
        ps.setString(7, i.getIcon());
        ps.setString(8, i.getRoomId());
        ps.setInt(9, i.getMinLevel());
        ps.setString(10, i.getModifierIds());
        ps.setString(11, i.getName());
        ps.setString(12, i.getStatText());
        ps.setInt(13, i.getTier());
        ps.setString(14, i.getUpgradeRoomId());
    }

    private IncursionRooms mapRow(ResultSet rs) throws SQLException {
        IncursionRooms i = new IncursionRooms();
        i.setPageId(rs.getInt("page_id"));
        i.setPageName(rs.getString("page_name"));
        i.setArchitectMetadataId(rs.getString("architect_metadata_id"));
        i.setArchitectName(rs.getString("architect_name"));
        i.setDescription(rs.getString("description"));
        i.setFlavourText(rs.getString("flavour_text"));
        i.setIcon(rs.getString("icon"));
        i.setRoomId(rs.getString("room_id"));
        i.setMinLevel(rs.getInt("min_level"));
        i.setModifierIds(rs.getString("modifier_ids"));
        i.setName(rs.getString("name"));
        i.setStatText(rs.getString("stat_text"));
        i.setTier(rs.getInt("tier"));
        i.setUpgradeRoomId(rs.getString("upgrade_room_id"));
        return i;
    }
}
