package com.poe.cache.dao;

import com.poe.cache.model.Version;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VersionDao implements CrudRepository<Version, Integer> {

    private final Connection connection;

    public VersionDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void insert(Version entity) {
        String sql = "INSERT INTO versions (page_id, page_name, after, major_part, minor_part, patch_part, " +
            "previous, release_date, revision_part, version) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParams(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert versions: " + entity.getPageId(), e);
        }
    }

    @Override
    public void batchInsert(List<Version> entities) {
        String sql = "INSERT INTO versions (page_id, page_name, after, major_part, minor_part, patch_part, " +
            "previous, release_date, revision_part, version) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (Version entity : entities) {
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
            throw new RuntimeException("Failed to batch insert versions", e);
        }
    }

    @Override
    public Optional<Version> findById(Integer pageId) {
        String sql = "SELECT * FROM versions WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find versions by id: " + pageId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Version> findAll() {
        List<Version> list = new ArrayList<>();
        String sql = "SELECT * FROM versions ORDER BY page_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all versions", e);
        }
        return list;
    }

    @Override
    public void deleteById(Integer pageId) {
        String sql = "DELETE FROM versions WHERE page_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete versions: " + pageId, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM versions";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count versions", e);
        }
        return 0;
    }

    private void setParams(PreparedStatement ps, Version v) throws SQLException {
        ps.setInt(1, v.getPageId());
        ps.setString(2, v.getPageName());
        ps.setString(3, v.getAfter());
        ps.setInt(4, v.getMajorPart());
        ps.setInt(5, v.getMinorPart());
        ps.setInt(6, v.getPatchPart());
        ps.setString(7, v.getPrevious());
        ps.setString(8, v.getReleaseDate());
        ps.setString(9, v.getRevisionPart());
        ps.setString(10, v.getVersion());
    }

    private Version mapRow(ResultSet rs) throws SQLException {
        Version v = new Version();
        v.setPageId(rs.getInt("page_id"));
        v.setPageName(rs.getString("page_name"));
        v.setAfter(rs.getString("after"));
        v.setMajorPart(rs.getInt("major_part"));
        v.setMinorPart(rs.getInt("minor_part"));
        v.setPatchPart(rs.getInt("patch_part"));
        v.setPrevious(rs.getString("previous"));
        v.setReleaseDate(rs.getString("release_date"));
        v.setRevisionPart(rs.getString("revision_part"));
        v.setVersion(rs.getString("version"));
        return v;
    }
}
