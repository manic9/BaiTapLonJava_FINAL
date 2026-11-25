package com.esports.manager.repository.sqlserver;

import com.esports.manager.config.DatabaseConnection;
import com.esports.manager.config.TransactionManager;
import com.esports.manager.model.Registration;
import com.esports.manager.model.enums.RegistrationStatus;
import com.esports.manager.repository.Repository;
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
import com.esports.manager.util.IdGenerator;

public class SqlServerRegistrationRepository implements Repository<Registration> {

    private static final String SELECT_ALL_SQL = """
            SELECT Id, TournamentId, TeamId, Seed, Status, Notes, RegisteredAt
            FROM dbo.Registrations
            ORDER BY RegisteredAt DESC
            """;

    private static final String SELECT_BY_ID_SQL = """
            SELECT Id, TournamentId, TeamId, Seed, Status, Notes, RegisteredAt
            FROM dbo.Registrations
            WHERE Id = ?
            """;

    private static final String UPSERT_SQL = """
            IF EXISTS (SELECT 1 FROM dbo.Registrations WHERE Id = ?)
            BEGIN
                UPDATE dbo.Registrations
                SET TournamentId = ?,
                    TeamId = ?,
                    Seed = ?,
                    Status = ?,
                    Notes = ?,
                    RegisteredAt = ?,
                    UpdatedAt = SYSDATETIME()
                WHERE Id = ?
            END
            ELSE
            BEGIN
                INSERT INTO dbo.Registrations
                    (Id, TournamentId, TeamId, Seed, Status, Notes, RegisteredAt, UpdatedAt)
                VALUES (?, ?, ?, ?, ?, ?, ?, SYSDATETIME())
            END
            """;

    private final DatabaseConnection dbConnection;
    private final TransactionManager transactionManager;
    private final ObservableList<Registration> cache = FXCollections.observableArrayList();

    public SqlServerRegistrationRepository(DatabaseConnection dbConnection, TransactionManager transactionManager) {
        this.dbConnection = dbConnection;
        this.transactionManager = transactionManager;
        reloadCache();
    }

    public final void reloadCache() {
        List<Registration> fresh = queryAllFromDb();
        cache.setAll(fresh);
    }

    private List<Registration> queryAllFromDb() {
        List<Registration> list = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải danh sách đăng ký từ SQL Server: "
                    + DatabaseConnection.translateSqlException(e), e);
        }
        return list;
    }

    @Override
    public ObservableList<Registration> findAll() {
        reloadCache();
        return cache;
    }

    @Override
    public Optional<Registration> findById(String id) {
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
            throw new IllegalStateException("Không thể tìm đơn đăng ký theo ID: "
                    + DatabaseConnection.translateSqlException(e), e);
        }
    }

    @Override
    public Registration save(Registration entity) {
        if (entity.getId() == null || entity.getId().isBlank()) {
            entity.setId(IdGenerator.next("REG"));
        }

        LocalDateTime regTime = entity.getRegisteredAt() != null ? entity.getRegisteredAt() : LocalDateTime.now();
        String statusStr = entity.getStatus() != null ? entity.getStatus().name() : RegistrationStatus.PENDING.name();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPSERT_SQL)) {

            // IF EXISTS
            ps.setString(1, entity.getId());

            // UPDATE
            ps.setString(2, entity.getTournamentId());
            ps.setString(3, entity.getTeamId());
            if (entity.getSeedNumber() >= 1) {
                ps.setInt(4, entity.getSeedNumber());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setString(5, statusStr);
            setNullableString(ps, 6, entity.getNotes());
            ps.setTimestamp(7, Timestamp.valueOf(regTime));
            ps.setString(8, entity.getId());

            // INSERT
            ps.setString(9, entity.getId());
            ps.setString(10, entity.getTournamentId());
            ps.setString(11, entity.getTeamId());
            if (entity.getSeedNumber() >= 1) {
                ps.setInt(12, entity.getSeedNumber());
            } else {
                ps.setNull(12, Types.INTEGER);
            }
            ps.setString(13, statusStr);
            setNullableString(ps, 14, entity.getNotes());
            ps.setTimestamp(15, Timestamp.valueOf(regTime));

            ps.executeUpdate();
            reloadCache();
            return entity;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể lưu đơn đăng ký vào SQL Server: "
                    + DatabaseConnection.translateSqlException(e), e);
        }
    }

    @Override
    public boolean deleteById(String id) {
        if (id == null || id.isBlank()) {
            return false;
        }
        String deleteSql = "DELETE FROM dbo.Registrations WHERE Id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(deleteSql)) {
            ps.setString(1, id);
            boolean deleted = ps.executeUpdate() > 0;
            if (deleted) {
                reloadCache();
            }
            return deleted;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể xóa đơn đăng ký trên SQL Server: "
                    + DatabaseConnection.translateSqlException(e), e);
        }
    }

    @Override
    public void clear() {
        String deleteAllSql = "DELETE FROM dbo.Registrations";
        try {
            transactionManager.executeVoidInTransaction(conn -> {
                try (PreparedStatement ps = conn.prepareStatement(deleteAllSql)) {
                    ps.executeUpdate();
                }
            });
            cache.clear();
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể xóa toàn bộ đơn đăng ký: "
                    + DatabaseConnection.translateSqlException(e), e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM dbo.Registrations";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể đếm số lượng đơn đăng ký: "
                    + DatabaseConnection.translateSqlException(e), e);
        }
    }

    private Registration mapRow(ResultSet rs) throws SQLException {
        Registration r = new Registration();
        r.setId(rs.getString("Id"));
        r.setTournamentId(rs.getString("TournamentId"));
        r.setTeamId(rs.getString("TeamId"));
        r.setSeedNumber(rs.getInt("Seed"));

        Timestamp ts = rs.getTimestamp("RegisteredAt");
        if (ts != null) {
            r.setRegisteredAt(ts.toLocalDateTime());
        }

        r.setStatus(parseEnum(RegistrationStatus.class, rs.getString("Status"), RegistrationStatus.PENDING));
        r.setNotes(rs.getString("Notes"));
        return r;
    }

    private static <E extends Enum<E>> E parseEnum(Class<E> enumType, String raw, E fallback) {
        if (raw == null || raw.isBlank()) return fallback;
        try {
            return Enum.valueOf(enumType, raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    private static void setNullableString(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null || value.isBlank()) {
            ps.setNull(index, Types.NVARCHAR);
        } else {
            ps.setString(index, value.trim());
        }
    }
}
