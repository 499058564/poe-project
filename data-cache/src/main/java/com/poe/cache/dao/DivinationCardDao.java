package com.poe.cache.dao;

import com.poe.cache.model.DivinationCard;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * divination_cards 表数据访问对象。
 */
public class DivinationCardDao implements CrudRepository<DivinationCard, Integer> {

    private final Connection connection;

    public DivinationCardDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void insert(DivinationCard dc) {
        String sql = "INSERT INTO divination_cards (page_id, page_name, card_art, card_background) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParams(ps, dc);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert divination card: " + dc.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<DivinationCard> cards) {
        String sql = "INSERT INTO divination_cards (page_id, page_name, card_art, card_background) VALUES (?, ?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (DivinationCard dc : cards) {
                    setParams(ps, dc);
                    ps.addBatch();
                }
                ps.executeBatch();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to batch insert divination cards", e);
        }
    }

    @Override
    public Optional<DivinationCard> findById(Integer pageId) {
        String sql = "SELECT * FROM divination_cards WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find divination card: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<DivinationCard> findAll() {
        List<DivinationCard> list = new ArrayList<>();
        String sql = "SELECT * FROM divination_cards ORDER BY page_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all divination cards", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM divination_cards WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete divination card: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM divination_cards";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count divination cards", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, DivinationCard dc) throws SQLException {
        ps.setInt(1, dc.getPageId());
        ps.setString(2, dc.getPageName());
        ps.setString(3, dc.getCardArt());
        ps.setString(4, dc.getCardBackground());
    }

    private DivinationCard mapRow(ResultSet rs) throws SQLException {
        DivinationCard dc = new DivinationCard();
        dc.setPageId(rs.getInt("page_id"));
        dc.setPageName(rs.getString("page_name"));
        dc.setCardArt(rs.getString("card_art"));
        dc.setCardBackground(rs.getString("card_background"));
        return dc;
    }
}
