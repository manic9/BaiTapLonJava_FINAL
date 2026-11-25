package com.esports.manager.repository.sqlserver;

import com.esports.manager.config.DatabaseConnection;
import com.esports.manager.config.TransactionManager;
import com.esports.manager.model.Tournament;
import com.esports.manager.model.enums.BracketFormat;
import com.esports.manager.model.enums.TournamentStatus;
import com.esports.manager.repository.Repository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.esports.manager.util.IdGenerator;

public class SqlServerTournamentRepository implements Repository<Tournament> {

    private static final String SELECT_ALL_SQL_TEMPLATE = """
            SELECT Id, Name, __GAME_COLUMN__ AS Game, Organizer, Format, Status,
                   MaxTeams, PrizePool, StartDate, EndDate, Location, Description
            FROM dbo.Tournaments
            ORDER BY StartDate DESC, CreatedAt DESC
            """;

    private static final String SELECT_BY_ID_SQL_TEMPLATE = """
            SELECT Id, Name, __GAME_COLUMN__ AS Game, Organizer, Format, Status,
                   MaxTeams, PrizePool, StartDate, EndDate, Location, Description
            FROM dbo.Tournaments
            WHERE Id = ?
            """;

    private static final String UPSERT_SQL_TEMPLATE = """
            IF EXISTS (SELECT 1 FROM dbo.Tournaments WHERE Id = ?)
            BEGIN
                UPDATE dbo.Tournaments
                SET Name = ?,
                    __GAME_COLUMN__ = ?,
                    Organizer = ?,
                    Format = ?,
                    Status = ?,
                    MaxTeams = ?,
                    PrizePool = ?,
                    StartDate = ?,
                    EndDate = ?,
                    Location = ?,
                    Description = ?,
                    UpdatedAt = SYSDATETIME()
                WHERE Id = ?
            END
            ELSE
            BEGIN
                INSERT INTO dbo.Tournaments
                    (Id, Name, __GAME_COLUMN__, Organizer, Format, Status,
                     MaxTeams, PrizePool, StartDate, EndDate, Location, Description, CreatedAt, UpdatedAt)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATETIME(), SYSDATETIME())
            END
            """;

    private final DatabaseConnection dbConnection;
    private final TransactionManager transactionManager;
    private final ObservableList<Tournament> cache = FXCollections.observableArrayList();
    private final String selectAllSql;
    private final String selectByIdSql;
    private final String upsertSql;

    public SqlServerTournamentRepository(DatabaseConnection dbConnection, TransactionManager transactionManager) {
        this.dbConnection = dbConnection;
        this.transactionManager = transactionManager;
        String gameColumn = resolveGameColumn();
        this.selectAllSql = sqlWithGameColumn(SELECT_ALL_SQL_TEMPLATE, gameColumn);
        this.selectByIdSql = sqlWithGameColumn(SELECT_BY_ID_SQL_TEMPLATE, gameColumn);
        this.upsertSql = sqlWithGameColumn(UPSERT_SQL_TEMPLATE, gameColumn);
        reloadCache();
    }

    /** Supports databases created with either the current Game or legacy GameTitle column. */
    private String resolveGameColumn() {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT TOP (0) * FROM dbo.Tournaments");
             ResultSet rs = ps.executeQuery()) {
            ResultSetMetaData metadata = rs.getMetaData();
            for (int i = 1; i <= metadata.getColumnCount(); i++) {
                String column = metadata.getColumnName(i);
                if ("Game".equalsIgnoreCase(column)) return "Game";
                if ("GameTitle".equalsIgnoreCase(column)) return "GameTitle";
            }
            throw new IllegalStateException("dbo.Tournaments không có cột Game hoặc GameTitle.");
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể kiểm tra cấu trúc dbo.Tournaments: "
                    + DatabaseConnection.translateSqlException(e), e);
        }
    }

    private static String sqlWithGameColumn(String template, String column) {
        // `column` comes only from the two names allowed by resolveGameColumn().
        return template.replace("__GAME_COLUMN__", "[" + column + "]");
    }

    public final void reloadCache() {
        List<Tournament> fresh = queryAllFromDb();
        cache.setAll(fresh);
    }

    private List<Tournament> queryAllFromDb() {
        List<Tournament> list = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(selectAllSql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải danh sách giải đấu từ SQL Server: "
                    + DatabaseConnection.translateSqlException(e), e);
        }
        return list;
    }

    @Override
    public ObservableList<Tournament> findAll() {
        reloadCache();
        return cache;
    }

    @Override
    public Optional<Tournament> findById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(selectByIdSql)) {

            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tìm giải đấu theo ID: "
                    + DatabaseConnection.translateSqlException(e), e);
        }
    }

    @Override
    public Tournament save(Tournament entity) {
        if (entity.getId() == null || entity.getId().isBlank()) {
            entity.setId(IdGenerator.next("TRN"));
        }

        String formatStr = entity.getFormat() != null
                ? entity.getFormat().name()
                : BracketFormat.values()[0].name();
        String statusStr = entity.getStatus() != null
                ? entity.getStatus().name()
                : TournamentStatus.values()[0].name();
        LocalDate start = entity.getStartDate() != null ? entity.getStartDate() : LocalDate.now();
        LocalDate end = entity.getEndDate() != null && !entity.getEndDate().isBefore(start)
                ? entity.getEndDate()
                : start;
        int maxTeams = Math.max(entity.getMaxTeams(), 2);
        BigDecimal prizePool = BigDecimal.valueOf(Math.max(entity.getPrizePool(), 0.0));
        String game = (entity.getGameTitle() != null && !entity.getGameTitle().isBlank())
                ? entity.getGameTitle()
                : "League of Legends";
        String organizer = entity.getOrganizer() != null && !entity.getOrganizer().isBlank()
                ? entity.getOrganizer().trim() : "Nexus Arena";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(upsertSql)) {

            // IF EXISTS
            ps.setString(1, entity.getId());

            // UPDATE
            ps.setString(2, entity.getName());
            ps.setString(3, game);
            ps.setString(4, organizer);
            ps.setString(5, formatStr);
            ps.setString(6, statusStr);
            ps.setInt(7, maxTeams);
            ps.setBigDecimal(8, prizePool);
            ps.setDate(9, Date.valueOf(start));
            ps.setDate(10, Date.valueOf(end));
            setNullableString(ps, 11, entity.getLocation());
            setNullableString(ps, 12, entity.getDescription());
            ps.setString(13, entity.getId());

            // INSERT
            ps.setString(14, entity.getId());
            ps.setString(15, entity.getName());
            ps.setString(16, game);
            ps.setString(17, organizer);
            ps.setString(18, formatStr);
            ps.setString(19, statusStr);
            ps.setInt(20, maxTeams);
            ps.setBigDecimal(21, prizePool);
            ps.setDate(22, Date.valueOf(start));
            ps.setDate(23, Date.valueOf(end));
            setNullableString(ps, 24, entity.getLocation());
            setNullableString(ps, 25, entity.getDescription());

            ps.executeUpdate();
            reloadCache();
            return entity;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể lưu giải đấu vào SQL Server: "
                    + DatabaseConnection.translateSqlException(e), e);
        }
    }

    @Override
    public boolean deleteById(String id) {
        if (id == null || id.isBlank()) {
            return false;
        }

        String unlinkMatchesSql = "UPDATE dbo.Matches SET NextMatchId = NULL, LoserNextMatchId = NULL WHERE TournamentId = ?";
        String deleteSql = "DELETE FROM dbo.Tournaments WHERE Id = ?";

        try {
            boolean deleted = transactionManager.executeInTransaction(conn -> {
                try (PreparedStatement psUnlink = conn.prepareStatement(unlinkMatchesSql)) {
                    psUnlink.setString(1, id);
                    psUnlink.executeUpdate();
                } catch (SQLException ignored) {}

                try (PreparedStatement psDelete = conn.prepareStatement(deleteSql)) {
                    psDelete.setString(1, id);
                    return psDelete.executeUpdate() > 0;
                }
            });
            if (deleted) {
                reloadCache();
            }
            return deleted;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể xóa giải đấu trên SQL Server: "
                    + DatabaseConnection.translateSqlException(e), e);
        }
    }

    @Override
    public void clear() {
        String unlinkMatchesSql = "UPDATE dbo.Matches SET NextMatchId = NULL, LoserNextMatchId = NULL";
        String deleteAllSql = "DELETE FROM dbo.Tournaments";

        try {
            transactionManager.executeVoidInTransaction(conn -> {
                try (PreparedStatement ps1 = conn.prepareStatement(unlinkMatchesSql)) {
                    ps1.executeUpdate();
                } catch (SQLException ignored) {}

                try (PreparedStatement ps2 = conn.prepareStatement(deleteAllSql)) {
                    ps2.executeUpdate();
                }
            });
            cache.clear();
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể xóa toàn bộ giải đấu: "
                    + DatabaseConnection.translateSqlException(e), e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM dbo.Tournaments";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể đếm số lượng giải đấu: "
                    + DatabaseConnection.translateSqlException(e), e);
        }
    }

    private Tournament mapRow(ResultSet rs) throws SQLException {
        Tournament t = new Tournament();
        t.setId(rs.getString("Id"));
        t.setName(rs.getString("Name"));
        t.setGameTitle(rs.getString("Game"));
        t.setOrganizer(rs.getString("Organizer"));
        t.setFormat(parseEnum(BracketFormat.class, rs.getString("Format"), BracketFormat.values()[0]));
        t.setStatus(parseEnum(TournamentStatus.class, rs.getString("Status"), TournamentStatus.values()[0]));
        t.setMaxTeams(rs.getInt("MaxTeams"));

        BigDecimal pool = rs.getBigDecimal("PrizePool");
        t.setPrizePool(pool != null ? pool.doubleValue() : 0.0);

        Date start = rs.getDate("StartDate");
        if (start != null) t.setStartDate(start.toLocalDate());

        Date end = rs.getDate("EndDate");
        if (end != null) t.setEndDate(end.toLocalDate());

        t.setLocation(rs.getString("Location"));
        t.setDescription(rs.getString("Description"));
        return t;
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
