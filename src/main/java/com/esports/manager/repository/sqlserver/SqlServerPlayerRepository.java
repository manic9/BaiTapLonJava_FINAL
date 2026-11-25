package com.esports.manager.repository.sqlserver;

import com.esports.manager.config.DatabaseConnection;
import com.esports.manager.config.TransactionManager;
import com.esports.manager.model.Player;
import com.esports.manager.repository.Repository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.esports.manager.util.IdGenerator;

public class SqlServerPlayerRepository implements Repository<Player> {

    private static final String SELECT_ALL_SQL = """
            SELECT Id, TeamId, FullName, Nickname, GameId, Role, Nationality, Age, IsCaptain, Status
            FROM dbo.Players
            ORDER BY Nickname ASC
            """;

    private static final String SELECT_BY_ID_SQL = """
            SELECT Id, TeamId, FullName, Nickname, GameId, Role, Nationality, Age, IsCaptain, Status
            FROM dbo.Players
            WHERE Id = ?
            """;

    private static final String UPSERT_SQL = """
            IF EXISTS (SELECT 1 FROM dbo.Players WHERE Id = ?)
            BEGIN
                UPDATE dbo.Players
                SET TeamId = ?,
                    FullName = ?,
                    Nickname = ?,
                    GameId = ?,
                    Role = ?,
                    Nationality = ?,
                    Age = ?,
                    IsCaptain = ?,
                    Status = ?,
                    UpdatedAt = SYSDATETIME()
                WHERE Id = ?
            END
            ELSE
            BEGIN
                INSERT INTO dbo.Players
                    (Id, TeamId, FullName, Nickname, GameId, Role, Nationality, Age, IsCaptain, Status, CreatedAt, UpdatedAt)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATETIME(), SYSDATETIME())
            END
            """;

    private final DatabaseConnection dbConnection;
    private final TransactionManager transactionManager;
    private final ObservableList<Player> cache = FXCollections.observableArrayList();

    public SqlServerPlayerRepository(DatabaseConnection dbConnection, TransactionManager transactionManager) {
        this.dbConnection = dbConnection;
        this.transactionManager = transactionManager;
        reloadCache();
    }

    public final void reloadCache() {
        List<Player> fresh = queryAllFromDb();
        cache.setAll(fresh);
    }

    private List<Player> queryAllFromDb() {
        List<Player> list = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải danh sách tuyển thủ từ SQL Server: "
                    + DatabaseConnection.translateSqlException(e), e);
        }
        return list;
    }

    @Override
    public ObservableList<Player> findAll() {
        reloadCache();
        return cache;
    }

    @Override
    public Optional<Player> findById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
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
            throw new IllegalStateException("Không thể tìm tuyển thủ theo ID: "
                    + DatabaseConnection.translateSqlException(e), e);
        }
    }

    @Override
    public Player save(Player entity) {
        if (entity.getId() == null || entity.getId().isBlank()) {
            entity.setId(IdGenerator.next("PLR"));
        }

        String fullName = (entity.getFullName() != null && !entity.getFullName().isBlank())
                ? entity.getFullName()
                : (entity.getInGameName() != null ? entity.getInGameName() : "Unknown Player");
        String nickname = (entity.getInGameName() != null && !entity.getInGameName().isBlank())
                ? entity.getInGameName()
                : fullName;
        String gameId = entity.getId();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPSERT_SQL)) {

            // IF EXISTS
            ps.setString(1, entity.getId());

            // UPDATE
            setNullableString(ps, 2, entity.getTeamId());
            ps.setString(3, fullName);
            ps.setString(4, nickname);
            ps.setString(5, gameId);
            setNullableString(ps, 6, entity.getRole());
            setNullableString(ps, 7, entity.getNationality());
            setNullableInt(ps, 8, entity.getAge());
            ps.setBoolean(9, entity.isCaptain());
            ps.setString(10, "ACTIVE");
            ps.setString(11, entity.getId());

            // INSERT
            ps.setString(12, entity.getId());
            setNullableString(ps, 13, entity.getTeamId());
            ps.setString(14, fullName);
            ps.setString(15, nickname);
            ps.setString(16, gameId);
            setNullableString(ps, 17, entity.getRole());
            setNullableString(ps, 18, entity.getNationality());
            setNullableInt(ps, 19, entity.getAge());
            ps.setBoolean(20, entity.isCaptain());
            ps.setString(21, "ACTIVE");

            ps.executeUpdate();
            reloadCache();
            return entity;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể lưu tuyển thủ vào SQL Server: "
                    + DatabaseConnection.translateSqlException(e), e);
        }
    }

    @Override
    public boolean deleteById(String id) {
        if (id == null || id.isBlank()) {
            return false;
        }
        String deleteSql = "DELETE FROM dbo.Players WHERE Id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(deleteSql)) {
            ps.setString(1, id);
            boolean deleted = ps.executeUpdate() > 0;
            if (deleted) {
                reloadCache();
            }
            return deleted;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể xóa tuyển thủ trên SQL Server: "
                    + DatabaseConnection.translateSqlException(e), e);
        }
    }

    @Override
    public void clear() {
        String deleteAllSql = "DELETE FROM dbo.Players";
        try {
            transactionManager.executeVoidInTransaction(conn -> {
                try (PreparedStatement ps = conn.prepareStatement(deleteAllSql)) {
                    ps.executeUpdate();
                }
            });
            cache.clear();
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể xóa toàn bộ tuyển thủ: "
                    + DatabaseConnection.translateSqlException(e), e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM dbo.Players";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể đếm số lượng tuyển thủ: "
                    + DatabaseConnection.translateSqlException(e), e);
        }
    }

    private Player mapRow(ResultSet rs) throws SQLException {
        Player p = new Player();
        p.setId(rs.getString("Id"));
        p.setTeamId(rs.getString("TeamId"));
        p.setFullName(rs.getString("FullName"));
        p.setInGameName(rs.getString("Nickname"));
        p.setRole(rs.getString("Role"));
        p.setNationality(rs.getString("Nationality"));
        int age = rs.getInt("Age");
        p.setAge(rs.wasNull() ? 0 : age);
        p.setCaptain(rs.getBoolean("IsCaptain"));
        return p;
    }

    private static void setNullableString(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null || value.isBlank()) {
            ps.setNull(index, Types.NVARCHAR);
        } else {
            ps.setString(index, value.trim());
        }
    }

    private static void setNullableInt(PreparedStatement ps, int index, int value) throws SQLException {
        if (value <= 0) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }
}
