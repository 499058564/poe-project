package com.poe.cache.dao;

import com.poe.cache.model.DivinationCard;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * divination_cards 表数据访问对象。
 * <p>
 * 表使用 page_id 作为主键，通过 page_name 与 base_items 表关联。
 * 记录预言卡的卡面美术和背景信息。
 */
public class DivinationCardDao implements CrudRepository<DivinationCard, Integer> {

    private final javax.sql.DataSource dataSource;

    public DivinationCardDao(javax.sql.DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 插入单条预言卡记录。
     *
     * @param dc 预言卡实体
     */
    @Override
    public void insert(DivinationCard dc) {
        String sql = "INSERT INTO divination_cards (page_id, page_name, card_art, card_background) VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, dc);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert divination card: " + dc.getPageId(), e);
        }
    }

    /**
     * 批量插入预言卡记录（事务）。
     * <p>
     * 使用 JDBC batch + 事务保证原子性，失败自动回滚。
     *
     * @param cards 预言卡实体列表（非空）
     */
    @Override
    public void batchInsert(List<DivinationCard> cards) {
        String sql = "INSERT INTO divination_cards (page_id, page_name, card_art, card_background) VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
                        conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (DivinationCard dc : cards) {
                    setParams(ps, dc);
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
            throw new RuntimeException("Failed to batch insert divination cards", e);
        }
    }

    /**
     * 根据主键查询预言卡。
     *
     * @param pageId Wiki 页面 ID
     * @return 预言卡实体（可能为空）
     */
    @Override
    public Optional<DivinationCard> findById(Integer pageId) {
        String sql = "SELECT * FROM divination_cards WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find divination card: " + pageId, e);
        }
        return Optional.empty();
    }

    /**
     * 查询全部预言卡记录。
     *
     * @return 按 page_id 升序排列的预言卡列表
     */
    @Override
    public List<DivinationCard> findAll() {
        List<DivinationCard> list = new ArrayList<>();
        String sql = "SELECT * FROM divination_cards ORDER BY page_id";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all divination cards", e);
        }
        return list;
    }

    /**
     * 根据主键删除预言卡。
     *
     * @param pageId Wiki 页面 ID
     */
    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM divination_cards WHERE page_id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete divination card: " + pageId, e);
        }
    }

    /**
     * 统计预言卡记录总数。
     *
     * @return divination_cards 表行数
     */
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM divination_cards";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count divination cards", e);
        }
        return 0;
    }

    /** 设置 PreparedStatement 参数（绑定 DivinationCard 字段） */
    private void setParams(PreparedStatement ps, DivinationCard dc) throws SQLException {
        ps.setInt(1, dc.getPageId());
        ps.setString(2, dc.getPageName());
        ps.setString(3, dc.getCardArt());
        ps.setString(4, dc.getCardBackground());
    }

    /** 从 ResultSet 映射一行到 DivinationCard 实体 */
    private DivinationCard mapRow(ResultSet rs) throws SQLException {
        DivinationCard dc = new DivinationCard();
        dc.setPageId(rs.getInt("page_id"));
        dc.setPageName(rs.getString("page_name"));
        dc.setCardArt(rs.getString("card_art"));
        dc.setCardBackground(rs.getString("card_background"));
        return dc;
    }
}
