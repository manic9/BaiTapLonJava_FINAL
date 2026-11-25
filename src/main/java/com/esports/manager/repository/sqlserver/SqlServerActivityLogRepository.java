package com.esports.manager.repository.sqlserver;

import com.esports.manager.config.DatabaseConnection;
import com.esports.manager.config.TransactionManager;
import com.esports.manager.model.ActivityLog;
import com.esports.manager.repository.Repository;
import com.esports.manager.util.IdGenerator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqlServerActivityLogRepository implements Repository<ActivityLog> {

    private static final String SELECT_ALL_SQL = """
            SELECT Id, ActorUsername, ActionType, Description, CreatedAt
            FROM dbo.ActivityLogs
            ORDER BY CreatedAt DESC
            """;

    private static final String SELECT_BY_ID_SQL = """
            SELECT Id, ActorUsername, ActionType, Description, CreatedAt
            FROM dbo.ActivityLogs
            WHERE Id = ?
            """;

    private static final String INSERT_SQL = """
            INSERT INTO dbo.ActivityLogs (Id, ActorUsername, ActionType, Description, CreatedAt)
            VALUES (?, ?, ?, ?, ?)
            """;

    private final DatabaseConnection dbConnection;
    private final TransactionManager transactionManager;
    private final ObservableList<ActivityLog> cache = FXCollections.observableArrayList();

    public SqlServerActivityLogRepository(DatabaseConnection dbConnection, TransactionManager transactionManager) {
        this.dbConnection = dbConnection;
        this.transactionManager = transactionManager;
        reloadCache();
    }

    public final void reloadCache() {
        List<ActivityLog> fresh = queryAllFromDb();
        cache.setAll(fresh);
    }

    private List<ActivityLog> queryAllFromDb() {
        List<ActivityLog> list = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải nhật ký hoạt động từ SQL Server: "
                    + DatabaseConnection.translateSqlException(e), e);
        }
        return list;
    }

    @Override
    public ObservableList<ActivityLog> findAll() {
        return cache;
    }

    @Override
    public Optional<ActivityLog> findById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        for (ActivityLog cached : cache) {
            if (id.equals(cached.getId())) {
                return Optional.of(cached);
            }
        }
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID_SQL)) {

            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tìm nhật ký hoạt động theo ID: "
                    + DatabaseConnection.translateSqlException(e), e);
        }
    }

    @Override
    public ActivityLog save(ActivityLog entity) {
        String id = entity.getId() != null ? entity.getId() : IdGenerator.next("LOG");
        LocalDateTime ts = entity.getTimestamp() != null ? entity.getTimestamp() : LocalDateTime.now();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {

            ps.setString(1, id);
            setNullableString(ps, 2, entity.getActor());
            ps.setString(3, entity.getCategory() != null ? entity.getCategory() : "SYSTEM");
            ps.setString(4, entity.getMessage() != null ? entity.getMessage() : "");
            ps.setTimestamp(5, Timestamp.valueOf(ts));

            ps.executeUpdate();
            reloadCache();
            return entity;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể lưu nhật ký hoạt động vào SQL Server: "
                    + DatabaseConnection.translateSqlException(e), e);
        }
    }

    @Override
    public boolean deleteById(String id) {
        if (id == null || id.isBlank()) {
            return false;
        }
        String deleteSql = "DELETE FROM dbo.ActivityLogs WHERE Id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(deleteSql)) {
            ps.setString(1, id);
            boolean deleted = ps.executeUpdate() > 0;
            if (deleted) {
                reloadCache();
            }
            return deleted;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể xóa nhật ký trên SQL Server: "
                    + DatabaseConnection.translateSqlException(e), e);
        }
    }

    @Override
    public void clear() {
        String deleteAllSql = "DELETE FROM dbo.ActivityLogs";
        try {
            transactionManager.executeVoidInTransaction(conn -> {
                try (PreparedStatement ps = conn.prepareStatement(deleteAllSql)) {
                    ps.executeUpdate();
                }
            });
            cache.clear();
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể xóa toàn bộ nhật ký: "
                    + DatabaseConnection.translateSqlException(e), e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM dbo.ActivityLogs";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể đếm số lượng nhật ký: "
                    + DatabaseConnection.translateSqlException(e), e);
        }
    }

    private ActivityLog mapRow(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("CreatedAt");
        LocalDateTime ldt = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
        String actionType = rs.getString("ActionType");
        String desc = rs.getString("Description");
        String actor = rs.getString("ActorUsername");

        return new ActivityLog(ldt, actionType, desc, actor);
    }

    private static void setNullableString(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null || value.isBlank()) {
            ps.setNull(index, Types.NVARCHAR);
        } else {
            ps.setString(index, value.trim());
        }
    }
}
