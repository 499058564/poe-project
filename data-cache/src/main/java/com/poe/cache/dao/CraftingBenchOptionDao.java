package com.poe.cache.dao;

import com.poe.cache.model.CraftingBenchOption;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * crafting_bench_options 表数据访问对象。
 * <p>
 * 表使用 option_id（Cargo 的 id 字段）作为主键。
 * 记录藏身处工艺台可制作的词缀选项。
 */
public class CraftingBenchOptionDao implements CrudRepository<CraftingBenchOption, Integer> {

    private final Connection connection;

    public CraftingBenchOptionDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void insert(CraftingBenchOption entity) {
        String sql = "INSERT INTO crafting_bench_options (page_id, page_name, option_id, " +
            "affix_type, unlock_category, unlock_category_description, description, " +
            "item_class_categories, item_classes, item_classes_ids, links, mod_group, " +
            "mod_id, name, npc, ordinal, rank, recipe_unlock_location, required_level, " +
            "socket_colours, sockets, unveils_required) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert crafting_bench_option: " + entity.getOptionId(), e);
        }
    }

    @Override
    public void batchInsert(List<CraftingBenchOption> entities) {
        String sql = "INSERT INTO crafting_bench_options (page_id, page_name, option_id, " +
            "affix_type, unlock_category, unlock_category_description, description, " +
            "item_class_categories, item_classes, item_classes_ids, links, mod_group, " +
            "mod_id, name, npc, ordinal, rank, recipe_unlock_location, required_level, " +
            "socket_colours, sockets, unveils_required) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (CraftingBenchOption entity : entities) {
                    setParams(ps, entity);
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
            throw new RuntimeException("Failed to batch insert crafting_bench_options", e);
        }
    }

    @Override
    public Optional<CraftingBenchOption> findById(Integer optionId) {
        String sql = "SELECT * FROM crafting_bench_options WHERE option_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, optionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find crafting_bench_option: " + optionId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<CraftingBenchOption> findAll() {
        List<CraftingBenchOption> list = new ArrayList<>();
        String sql = "SELECT * FROM crafting_bench_options ORDER BY option_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all crafting_bench_options", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer optionId) {
        String sql = "DELETE FROM crafting_bench_options WHERE option_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, optionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete crafting_bench_option: " + optionId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM crafting_bench_options";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count crafting_bench_options", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, CraftingBenchOption o) throws SQLException {
        int idx = 1;
        ps.setInt(idx++, o.getPageId());
        ps.setString(idx++, o.getPageName());
        ps.setInt(idx++, o.getOptionId());
        ps.setString(idx++, o.getAffixType());
        ps.setString(idx++, o.getUnlockCategory());
        ps.setString(idx++, o.getUnlockCategoryDescription());
        ps.setString(idx++, o.getDescription());
        ps.setString(idx++, o.getItemClassCategories());
        ps.setString(idx++, o.getItemClasses());
        ps.setString(idx++, o.getItemClassesIds());
        ps.setInt(idx++, o.getLinks());
        ps.setString(idx++, o.getModGroup());
        ps.setString(idx++, o.getModId());
        ps.setString(idx++, o.getName());
        ps.setString(idx++, o.getNpc());
        ps.setInt(idx++, o.getOrdinal());
        ps.setInt(idx++, o.getRank());
        ps.setString(idx++, o.getRecipeUnlockLocation());
        ps.setInt(idx++, o.getRequiredLevel());
        ps.setString(idx++, o.getSocketColours());
        ps.setInt(idx++, o.getSockets());
        ps.setInt(idx++, o.getUnveilsRequired());
    }

    private CraftingBenchOption mapRow(ResultSet rs) throws SQLException {
        CraftingBenchOption o = new CraftingBenchOption();
        o.setPageId(rs.getInt("page_id"));
        o.setPageName(rs.getString("page_name"));
        o.setOptionId(rs.getInt("option_id"));
        o.setAffixType(rs.getString("affix_type"));
        o.setUnlockCategory(rs.getString("unlock_category"));
        o.setUnlockCategoryDescription(rs.getString("unlock_category_description"));
        o.setDescription(rs.getString("description"));
        o.setItemClassCategories(rs.getString("item_class_categories"));
        o.setItemClasses(rs.getString("item_classes"));
        o.setItemClassesIds(rs.getString("item_classes_ids"));
        o.setLinks(rs.getInt("links"));
        o.setModGroup(rs.getString("mod_group"));
        o.setModId(rs.getString("mod_id"));
        o.setName(rs.getString("name"));
        o.setNpc(rs.getString("npc"));
        o.setOrdinal(rs.getInt("ordinal"));
        o.setRank(rs.getInt("rank"));
        o.setRecipeUnlockLocation(rs.getString("recipe_unlock_location"));
        o.setRequiredLevel(rs.getInt("required_level"));
        o.setSocketColours(rs.getString("socket_colours"));
        o.setSockets(rs.getInt("sockets"));
        o.setUnveilsRequired(rs.getInt("unveils_required"));
        return o;
    }
}
