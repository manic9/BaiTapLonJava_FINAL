package com.nexusarena.repository.sqlserver;

import com.nexusarena.db.DatabaseConnection;
import com.nexusarena.db.TransactionManager;
import com.nexusarena.exception.DataAccessException;
import com.nexusarena.model.Tournament;
import com.nexusarena.repository.TournamentRepository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SqlServerTournamentRepository implements TournamentRepository {

    private static final String BASE_SELECT = """
            SELECT t.Id, t.Name, t.Game, t.Format, t.MaxTeams,
                   t.StartDate, t.EndDate, t.Location, t.PrizePool,
                   t.Status, t.BannerUrl, t.Description, t.CreatedAt, t.UpdatedAt,
                   COUNT(r.Id) AS ApprovedTeamsCount
            FROM dbo.Tournaments t
            LEFT JOIN dbo.Registrations r
                ON r.TournamentId = t.Id AND r.Status = N'APPROVED'
            """;

    private static final String GROUP_BY_CLAUSE = """
            GROUP BY t.Id, t.Name, t.Game, t.Format, t.MaxTeams,
                     t.StartDate, t.EndDate, t.Location, t.PrizePool,
                     t.Status, t.BannerUrl, t.Description, t.CreatedAt, t.UpdatedAt
            """;

    private final DatabaseConnection dbConnection;
    private final TransactionManager transactionManager;

    public SqlServerTournamentRepository(DatabaseConnection dbConnection, TransactionManager transactionManager) {
        this.dbConnection = dbConnection;
        this.transactionManager = transactionManager;
    }

    @Override
    public List<Tournament> findAll() {
        String sql = BASE_SELECT + GROUP_BY_CLAUSE + " ORDER BY t.StartDate DESC, t.CreatedAt DESC";
        List<Tournament> list = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw DataAccessException.fromSqlException("Không thể tải danh sách giải đấu", e);
        }
    }

    @Override
    public Optional<Tournament> findById(String id) {
        String sql = BASE_SELECT + " WHERE t.Id = ? " + GROUP_BY_CLAUSE;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw DataAccessException.fromSqlException("Không thể tải chi tiết giải đấu", e);
        }
    }

    @Override
    public List<Tournament> searchAndFilter(String keyword, String game, String status) {
        StringBuilder sql = new StringBuilder(BASE_SELECT).append(" WHERE 1 = 1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (t.Name LIKE ? OR t.Location LIKE ? OR t.Game LIKE ?) ");
            String pattern = "%" + keyword.trim() + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }

        if (game != null && !game.isBlank() && !"ALL".equalsIgnoreCase(game)) {
            sql.append(" AND t.Game = ? ");
            params.add(game.trim());
        }

        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            sql.append(" AND t.Status = ? ");
            params.add(status.trim());
        }

        sql.append(GROUP_BY_CLAUSE).append(" ORDER BY t.StartDate DESC, t.CreatedAt DESC");

        List<Tournament> list = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw DataAccessException.fromSqlException("Lỗi khi tìm kiếm / lọc giải đấu", e);
        }
    }

    @Override
    public Tournament save(Tournament tournament) {
        if (tournament.getId() == null || tournament.getId().isBlank()) {
            tournament.setId("trn-" + UUID.randomUUID().toString().substring(0, 8));
        }

        String sql = """
                INSERT INTO dbo.Tournaments
                    (Id, Name, Game, Format, MaxTeams, StartDate, EndDate,
                     Location, PrizePool, Status, BannerUrl, Description, CreatedAt)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATETIME())
                """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tournament.getId());
            ps.setString(2, tournament.getName());
            ps.setString(3, tournament.getGame());
            ps.setString(4, tournament.getFormat());
            ps.setInt(5, tournament.getMaxTeams());
            ps.setDate(6, Date.valueOf(tournament.getStartDate()));
            ps.setDate(7, Date.valueOf(tournament.getEndDate()));
            setNullableString(ps, 8, tournament.getLocation());
            ps.setBigDecimal(9, tournament.getPrizePool() != null ? tournament.getPrizePool() : BigDecimal.ZERO);
            ps.setString(10, tournament.getStatus());
            setNullableString(ps, 11, tournament.getBannerUrl());
            setNullableString(ps, 12, tournament.getDescription());

            ps.executeUpdate();
            return findById(tournament.getId()).orElse(tournament);
        } catch (SQLException e) {
            throw DataAccessException.fromSqlException("Không thể tạo giải đấu mới", e);
        }
    }

    @Override
    public Tournament update(Tournament tournament) {
        String sql = """
                UPDATE dbo.Tournaments
                SET Name = ?,
                    Game = ?,
                    Format = ?,
                    MaxTeams = ?,
                    StartDate = ?,
                    EndDate = ?,
                    Location = ?,
                    PrizePool = ?,
                    Status = ?,
                    BannerUrl = ?,
                    Description = ?,
                    UpdatedAt = SYSDATETIME()
                WHERE Id = ?
                """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tournament.getName());
            ps.setString(2, tournament.getGame());
            ps.setString(3, tournament.getFormat());
            ps.setInt(4, tournament.getMaxTeams());
            ps.setDate(5, Date.valueOf(tournament.getStartDate()));
            ps.setDate(6, Date.valueOf(tournament.getEndDate()));
            setNullableString(ps, 7, tournament.getLocation());
            ps.setBigDecimal(8, tournament.getPrizePool() != null ? tournament.getPrizePool() : BigDecimal.ZERO);
            ps.setString(9, tournament.getStatus());
            setNullableString(ps, 10, tournament.getBannerUrl());
            setNullableString(ps, 11, tournament.getDescription());
            ps.setString(12, tournament.getId());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new DataAccessException("Không tìm thấy giải đấu cần cập nhật (ID: " + tournament.getId() + ").");
            }
            return findById(tournament.getId()).orElse(tournament);
        } catch (SQLException e) {
            throw DataAccessException.fromSqlException("Không thể cập nhật thông tin giải đấu", e);
        }
    }

    @Override
    public boolean updateStatus(String id, String newStatus) {
        String sql = "UPDATE dbo.Tournaments SET Status = ?, UpdatedAt = SYSDATETIME() WHERE Id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setString(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw DataAccessException.fromSqlException("Không thể cập nhật trạng thái giải đấu", e);
        }
    }

    @Override
    public boolean deleteById(String id) {
        String unlinkSelfMatchesSql = """
                UPDATE dbo.Matches
                SET NextMatchId = NULL, LoserNextMatchId = NULL
                WHERE TournamentId = ?
                """;
        String deleteTournamentSql = "DELETE FROM dbo.Tournaments WHERE Id = ?";

        try {
            return transactionManager.executeInTransaction(conn -> {
                try (PreparedStatement psUnlink = conn.prepareStatement(unlinkSelfMatchesSql)) {
                    psUnlink.setString(1, id);
                    psUnlink.executeUpdate();
                }

                try (PreparedStatement psDelete = conn.prepareStatement(deleteTournamentSql)) {
                    psDelete.setString(1, id);
                    return psDelete.executeUpdate() > 0;
                }
            });
        } catch (SQLException e) {
            throw DataAccessException.fromSqlException("Không thể xóa giải đấu", e);
        }
    }

    @Override
    public long countAll() {
        String sql = "SELECT COUNT(*) FROM dbo.Tournaments";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0L;
        } catch (SQLException e) {
            throw DataAccessException.fromSqlException("Lỗi đếm tổng số giải đấu", e);
        }
    }

    @Override
    public long countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM dbo.Tournaments WHERE Status = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        } catch (SQLException e) {
            throw DataAccessException.fromSqlException("Lỗi đếm giải đấu theo trạng thái", e);
        }
    }

    private Tournament mapRow(ResultSet rs) throws SQLException {
        Tournament t = new Tournament();
        t.setId(rs.getString("Id"));
        t.setName(rs.getString("Name"));
        t.setGame(rs.getString("Game"));
        t.setFormat(rs.getString("Format"));
        t.setMaxTeams(rs.getInt("MaxTeams"));

        Date start = rs.getDate("StartDate");
        if (start != null) t.setStartDate(start.toLocalDate());

        Date end = rs.getDate("EndDate");
        if (end != null) t.setEndDate(end.toLocalDate());

        t.setLocation(rs.getString("Location"));
        t.setPrizePool(rs.getBigDecimal("PrizePool"));
        t.setStatus(rs.getString("Status"));
        t.setBannerUrl(rs.getString("BannerUrl"));
        t.setDescription(rs.getString("Description"));

        Timestamp created = rs.getTimestamp("CreatedAt");
        if (created != null) t.setCreatedAt(created.toLocalDateTime());

        Timestamp updated = rs.getTimestamp("UpdatedAt");
        if (updated != null) t.setUpdatedAt(updated.toLocalDateTime());

        t.setApprovedTeamsCount(rs.getInt("ApprovedTeamsCount"));
        return t;
    }

    private void setNullableString(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null || value.isBlank()) {
            ps.setNull(index, Types.NVARCHAR);
        } else {
            ps.setString(index, value.trim());
        }
    }
}