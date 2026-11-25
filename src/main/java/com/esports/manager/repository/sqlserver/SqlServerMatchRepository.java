package com.esports.manager.repository.sqlserver;

import com.esports.manager.config.DatabaseConnection;
import com.esports.manager.config.TransactionManager;
import com.esports.manager.model.Match;
import com.esports.manager.model.enums.MatchStatus;
import com.esports.manager.repository.Repository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.esports.manager.util.IdGenerator;

public class SqlServerMatchRepository implements Repository<Match> {

    private static final String SELECT_ALL_SQL = """
            SELECT Id, TournamentId, BracketSide, RoundNumber, RoundName, MatchOrder,
                   Team1Id, Team2Id, Score1, Score2, WinnerTeamId, ScheduledAt, Status,
                   NextMatchId, NextMatchSlot, LoserNextMatchId, Notes
            FROM dbo.Matches
            ORDER BY RoundNumber ASC, MatchOrder ASC
            """;

    private static final String SELECT_BY_ID_SQL = """
            SELECT Id, TournamentId, BracketSide, RoundNumber, RoundName, MatchOrder,
                   Team1Id, Team2Id, Score1, Score2, WinnerTeamId, ScheduledAt, Status,
                   NextMatchId, NextMatchSlot, LoserNextMatchId, Notes
            FROM dbo.Matches
            WHERE Id = ?
            """;

    private static final String UPSERT_SQL = """
            IF EXISTS (SELECT 1 FROM dbo.Matches WHERE Id = ?)
            BEGIN
                UPDATE dbo.Matches
                SET TournamentId = ?,
                    BracketSide = ?,
                    RoundNumber = ?,
                    RoundName = ?,
                    MatchOrder = ?,
                    Team1Id = ?,
                    Team2Id = ?,
                    Score1 = ?,
                    Score2 = ?,
                    WinnerTeamId = ?,
                    ScheduledAt = ?,
                    Status = ?,
                    NextMatchId = ?,
                    NextMatchSlot = ?,
                    UpdatedAt = SYSDATETIME()
                WHERE Id = ?
            END
            ELSE
            BEGIN
                INSERT INTO dbo.Matches
                    (Id, TournamentId, BracketSide, RoundNumber, RoundName, MatchOrder,
                     Team1Id, Team2Id, Score1, Score2, WinnerTeamId, ScheduledAt, Status,
                     NextMatchId, NextMatchSlot, CreatedAt, UpdatedAt)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATETIME(), SYSDATETIME())
            END
            """;

    private final DatabaseConnection dbConnection;
    private final TransactionManager transactionManager;
    private final ObservableList<Match> cache = FXCollections.observableArrayList();

    public SqlServerMatchRepository(DatabaseConnection dbConnection, TransactionManager transactionManager) {
        this.dbConnection = dbConnection;
        this.transactionManager = transactionManager;
        reloadCache();
    }

    public final void reloadCache() {
        List<Match> fresh = queryAllFromDb();
        cache.setAll(fresh);
    }

    private List<Match> queryAllFromDb() {
        List<Match> list = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải danh sách trận đấu từ SQL Server: "
                    + DatabaseConnection.translateSqlException(e), e);
        }
        return list;
    }

    @Override
    public ObservableList<Match> findAll() {
        return cache;
    }

    @Override
    public Optional<Match> findById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        for (Match cached : cache) {
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
            throw new IllegalStateException("Không thể tìm trận đấu theo ID: "
                    + DatabaseConnection.translateSqlException(e), e);
        }
    }

    @Override
    public Match save(Match entity) {
        if (entity.getId() == null || entity.getId().isBlank()) {
            entity.setId(IdGenerator.next("MCH"));
        }

        String statusStr = entity.getStatus() != null ? entity.getStatus().name() : MatchStatus.SCHEDULED.name();
        int roundNumber = Math.max(entity.getRound(), 1);
        int matchOrder = Math.max(entity.getMatchNumber(), 1);
        String roundName = (entity.getRoundName() != null && !entity.getRoundName().isBlank())
                ? entity.getRoundName()
                : "Round " + roundNumber;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPSERT_SQL)) {

            // IF EXISTS
            ps.setString(1, entity.getId());

            // UPDATE
            ps.setString(2, entity.getTournamentId());
            ps.setString(3, "UPPER");
            ps.setInt(4, roundNumber);
            ps.setString(5, roundName);
            ps.setInt(6, matchOrder);
            setNullableString(ps, 7, entity.getTeamAId());
            setNullableString(ps, 8, entity.getTeamBId());
            ps.setInt(9, Math.max(entity.getScoreA(), 0));
            ps.setInt(10, Math.max(entity.getScoreB(), 0));
            setNullableString(ps, 11, entity.getWinnerId());
            if (entity.getScheduledTime() != null) {
                ps.setTimestamp(12, Timestamp.valueOf(entity.getScheduledTime()));
            } else {
                ps.setNull(12, Types.TIMESTAMP);
            }
            ps.setString(13, statusStr);
            setNullableString(ps, 14, entity.getNextMatchId());
            if (entity.getNextMatchSlot() == 1 || entity.getNextMatchSlot() == 2) {
                ps.setInt(15, entity.getNextMatchSlot());
            } else {
                ps.setNull(15, Types.INTEGER);
            }
            ps.setString(16, entity.getId());

            // INSERT
            ps.setString(17, entity.getId());
            ps.setString(18, entity.getTournamentId());
            ps.setString(19, "UPPER");
            ps.setInt(20, roundNumber);
            ps.setString(21, roundName);
            ps.setInt(22, matchOrder);
            setNullableString(ps, 23, entity.getTeamAId());
            setNullableString(ps, 24, entity.getTeamBId());
            ps.setInt(25, Math.max(entity.getScoreA(), 0));
            ps.setInt(26, Math.max(entity.getScoreB(), 0));
            setNullableString(ps, 27, entity.getWinnerId());
            if (entity.getScheduledTime() != null) {
                ps.setTimestamp(28, Timestamp.valueOf(entity.getScheduledTime()));
            } else {
                ps.setNull(28, Types.TIMESTAMP);
            }
            ps.setString(29, statusStr);
            setNullableString(ps, 30, entity.getNextMatchId());
            if (entity.getNextMatchSlot() == 1 || entity.getNextMatchSlot() == 2) {
                ps.setInt(31, entity.getNextMatchSlot());
            } else {
                ps.setNull(31, Types.INTEGER);
            }

            ps.executeUpdate();
            reloadCache();
            return entity;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể lưu trận đấu vào SQL Server: "
                    + DatabaseConnection.translateSqlException(e), e);
        }
    }

    @Override
    public boolean deleteById(String id) {
        if (id == null || id.isBlank()) {
            return false;
        }
        String unlinkSql = "UPDATE dbo.Matches SET NextMatchId = NULL WHERE NextMatchId = ?";
        String deleteSql = "DELETE FROM dbo.Matches WHERE Id = ?";
        try {
            boolean deleted = transactionManager.executeInTransaction(conn -> {
                try (PreparedStatement psU = conn.prepareStatement(unlinkSql)) {
                    psU.setString(1, id);
                    psU.executeUpdate();
                } catch (SQLException ignored) {}

                try (PreparedStatement psD = conn.prepareStatement(deleteSql)) {
                    psD.setString(1, id);
                    return psD.executeUpdate() > 0;
                }
            });
            if (deleted) {
                reloadCache();
            }
            return deleted;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể xóa trận đấu trên SQL Server: "
                    + DatabaseConnection.translateSqlException(e), e);
        }
    }

    @Override
    public void clear() {
        String unlinkSql = "UPDATE dbo.Matches SET NextMatchId = NULL, LoserNextMatchId = NULL";
        String deleteAllSql = "DELETE FROM dbo.Matches";
        try {
            transactionManager.executeVoidInTransaction(conn -> {
                try (PreparedStatement psU = conn.prepareStatement(unlinkSql)) {
                    psU.executeUpdate();
                } catch (SQLException ignored) {}

                try (PreparedStatement psD = conn.prepareStatement(deleteAllSql)) {
                    psD.executeUpdate();
                }
            });
            cache.clear();
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể xóa toàn bộ trận đấu: "
                    + DatabaseConnection.translateSqlException(e), e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM dbo.Matches";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể đếm số lượng trận đấu: "
                    + DatabaseConnection.translateSqlException(e), e);
        }
    }

    private Match mapRow(ResultSet rs) throws SQLException {
        Match m = new Match();
        m.setId(rs.getString("Id"));
        m.setTournamentId(rs.getString("TournamentId"));
        m.setRound(rs.getInt("RoundNumber"));
        m.setMatchNumber(rs.getInt("MatchOrder"));
        m.setRoundName(rs.getString("RoundName"));
        m.setTeamAId(rs.getString("Team1Id"));
        m.setTeamBId(rs.getString("Team2Id"));
        m.setScoreA(rs.getInt("Score1"));
        m.setScoreB(rs.getInt("Score2"));
        m.setWinnerId(rs.getString("WinnerTeamId"));
        m.setNextMatchId(rs.getString("NextMatchId"));
        m.setNextMatchSlot(rs.getInt("NextMatchSlot"));

        Timestamp ts = rs.getTimestamp("ScheduledAt");
        if (ts != null) {
            m.setScheduledTime(ts.toLocalDateTime());
        }

        m.setStatus(parseEnum(MatchStatus.class, rs.getString("Status"), MatchStatus.SCHEDULED));
        return m;
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
