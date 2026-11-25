package com.esports.manager.repository.sqlserver;

import com.esports.manager.config.DatabaseConnection;
import com.esports.manager.config.TransactionManager;
import com.esports.manager.model.PrizeAllocation;
import com.esports.manager.repository.Repository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.esports.manager.util.IdGenerator;

public class SqlServerPrizeAllocationRepository implements Repository<PrizeAllocation> {

    private static final String SELECT_ALL_SQL = """
            SELECT Id, TournamentId, RankPosition, RankTitle, Percentage, Amount, TeamId, DisbursementStatus
            FROM dbo.PrizeAllocations
            ORDER BY RankPosition ASC
            """;

    private static final String SELECT_BY_ID_SQL = """
            SELECT Id, TournamentId, RankPosition, RankTitle, Percentage, Amount, TeamId, DisbursementStatus
            FROM dbo.PrizeAllocations
            WHERE Id = ?
            """;

    private static final String UPSERT_SQL = """
            IF EXISTS (SELECT 1 FROM dbo.PrizeAllocations WHERE Id = ?)
            BEGIN
                UPDATE dbo.PrizeAllocations
                SET TournamentId = ?,
                    RankPosition = ?,
                    RankTitle = ?,
                    Percentage = ?,
                    Amount = ?,
                    TeamId = ?,
                    DisbursementStatus = ?,
                    UpdatedAt = SYSDATETIME()
                WHERE Id = ?
            END
            ELSE
            BEGIN
                INSERT INTO dbo.PrizeAllocations
                    (Id, TournamentId, RankPosition, RankTitle, Percentage, Amount, TeamId, DisbursementStatus, CreatedAt, UpdatedAt)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, SYSDATETIME(), SYSDATETIME())
            END
            """;

    private final DatabaseConnection dbConnection;
    private final TransactionManager transactionManager;
    private final ObservableList<PrizeAllocation> cache = FXCollections.observableArrayList();

    public SqlServerPrizeAllocationRepository(DatabaseConnection dbConnection, TransactionManager transactionManager) {
        this.dbConnection = dbConnection;
        this.transactionManager = transactionManager;
        reloadCache();
    }

    public final void reloadCache() {
        List<PrizeAllocation> fresh = queryAllFromDb();
        cache.setAll(fresh);
    }

    private List<PrizeAllocation> queryAllFromDb() {
        List<PrizeAllocation> list = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải danh sách giải thưởng từ SQL Server: "
                    + DatabaseConnection.translateSqlException(e), e);
        }
        return list;
    }

    @Override
    public ObservableList<PrizeAllocation> findAll() {
        return cache;
    }

    @Override
    public Optional<PrizeAllocation> findById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        for (PrizeAllocation cached : cache) {
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
            throw new IllegalStateException("Không thể tìm phân bổ giải thưởng theo ID: "
                    + DatabaseConnection.translateSqlException(e), e);
        }
    }

    @Override
    public PrizeAllocation save(PrizeAllocation entity) {
        if (entity.getId() == null || entity.getId().isBlank()) {
            entity.setId(IdGenerator.next("PRZ"));
        }

        String placement = (entity.getPlacement() != null && !entity.getPlacement().isBlank())
                ? entity.getPlacement()
                : "Placement";
        int rankPosition = 1;
        if (placement.contains("1") || placement.toLowerCase().contains("nhất") || placement.toLowerCase().contains("vô địch")) rankPosition = 1;
        else if (placement.contains("2") || placement.toLowerCase().contains("nhì") || placement.toLowerCase().contains("á quân")) rankPosition = 2;
        else if (placement.contains("3") || placement.toLowerCase().contains("ba")) rankPosition = 3;
        else if (placement.contains("4")) rankPosition = 4;
        else rankPosition = Math.max(cache.size() + 1, 1);

        String disbursementStatus = entity.isPaid() ? "PAID" : "UNPAID";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPSERT_SQL)) {

            // IF EXISTS
            ps.setString(1, entity.getId());

            // UPDATE
            ps.setString(2, entity.getTournamentId());
            ps.setInt(3, rankPosition);
            ps.setString(4, placement);
            ps.setBigDecimal(5, BigDecimal.valueOf(Math.min(Math.max(entity.getPercentage(), 0.0), 100.0)));
            ps.setBigDecimal(6, BigDecimal.valueOf(Math.max(entity.getAmount(), 0.0)));
            setNullableString(ps, 7, entity.getTeamId());
            ps.setString(8, disbursementStatus);
            ps.setString(9, entity.getId());

            // INSERT
            ps.setString(10, entity.getId());
            ps.setString(11, entity.getTournamentId());
            ps.setInt(12, rankPosition);
            ps.setString(13, placement);
            ps.setBigDecimal(14, BigDecimal.valueOf(Math.min(Math.max(entity.getPercentage(), 0.0), 100.0)));
            ps.setBigDecimal(15, BigDecimal.valueOf(Math.max(entity.getAmount(), 0.0)));
            setNullableString(ps, 16, entity.getTeamId());
            ps.setString(17, disbursementStatus);

            ps.executeUpdate();
            reloadCache();
            return entity;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể lưu giải thưởng vào SQL Server: "
                    + DatabaseConnection.translateSqlException(e), e);
        }
    }

    @Override
    public boolean deleteById(String id) {
        if (id == null || id.isBlank()) {
            return false;
        }
        String deleteSql = "DELETE FROM dbo.PrizeAllocations WHERE Id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(deleteSql)) {
            ps.setString(1, id);
            boolean deleted = ps.executeUpdate() > 0;
            if (deleted) {
                reloadCache();
            }
            return deleted;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể xóa giải thưởng trên SQL Server: "
                    + DatabaseConnection.translateSqlException(e), e);
        }
    }

    @Override
    public void clear() {
        String deleteAllSql = "DELETE FROM dbo.PrizeAllocations";
        try {
            transactionManager.executeVoidInTransaction(conn -> {
                try (PreparedStatement ps = conn.prepareStatement(deleteAllSql)) {
                    ps.executeUpdate();
                }
            });
            cache.clear();
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể xóa toàn bộ giải thưởng: "
                    + DatabaseConnection.translateSqlException(e), e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM dbo.PrizeAllocations";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể đếm số lượng giải thưởng: "
                    + DatabaseConnection.translateSqlException(e), e);
        }
    }

    private PrizeAllocation mapRow(ResultSet rs) throws SQLException {
        PrizeAllocation p = new PrizeAllocation();
        p.setId(rs.getString("Id"));
        p.setTournamentId(rs.getString("TournamentId"));
        p.setPlacement(rs.getString("RankTitle"));

        BigDecimal pct = rs.getBigDecimal("Percentage");
        p.setPercentage(pct != null ? pct.doubleValue() : 0.0);

        BigDecimal amt = rs.getBigDecimal("Amount");
        p.setAmount(amt != null ? amt.doubleValue() : 0.0);

        p.setTeamId(rs.getString("TeamId"));
        p.setPaid("PAID".equalsIgnoreCase(rs.getString("DisbursementStatus")));
        return p;
    }

    private static void setNullableString(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null || value.isBlank()) {
            ps.setNull(index, Types.NVARCHAR);
        } else {
            ps.setString(index, value.trim());
        }
    }
}
