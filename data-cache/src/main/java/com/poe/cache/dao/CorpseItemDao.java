package com.poe.cache.dao;

import javax.sql.DataSource;
import com.poe.cache.model.CorpseItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CorpseItemDao implements CrudRepository<CorpseItem, Integer> {

    private final DataSource dataSource;

    public CorpseItemDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insert(CorpseItem entity) {
        String sql = "INSERT INTO corpse_items (page_id, page_name, monster_abilities, monster_category, " +
            "monster_category_html, tier) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert corpse_items: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<CorpseItem> entities) {
        String sql = "INSERT INTO corpse_items (page_id, page_name, monster_abilities, monster_category, " +
            "monster_category_html, tier) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (CorpseItem entity : entities) {
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
            throw new RuntimeException("Failed to batch insert corpse_items", e);
        }
    }

    @Override
    public Optional<CorpseItem> findById(Integer pageId) {
        String sql = "SELECT * FROM corpse_items WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find corpse_items by id: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<CorpseItem> findAll() {
        List<CorpseItem> list = new ArrayList<>();
        String sql = "SELECT * FROM corpse_items ORDER BY page_id";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all corpse_items", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM corpse_items WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete corpse_items: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM corpse_items";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count corpse_items", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, CorpseItem v) throws SQLException {
        ps.setInt(1, v.getPageId());
        ps.setString(2, v.getPageName());
        ps.setString(3, v.getMonsterAbilities());
        ps.setString(4, v.getMonsterCategory());
        ps.setString(5, v.getMonsterCategoryHtml());
        ps.setInt(6, v.getTier());
    }

    private CorpseItem mapRow(ResultSet rs) throws SQLException {
        CorpseItem v = new CorpseItem();
        v.setPageId(rs.getInt("page_id"));
        v.setPageName(rs.getString("page_name"));
        v.setMonsterAbilities(rs.getString("monster_abilities"));
        v.setMonsterCategory(rs.getString("monster_category"));
        v.setMonsterCategoryHtml(rs.getString("monster_category_html"));
        v.setTier(rs.getInt("tier"));
        return v;
    }
}
