package com.esports.manager.repository.sqlserver;

import com.esports.manager.config.DatabaseConnection;
import com.esports.manager.config.TransactionManager;
import com.esports.manager.model.Team;
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
import com.esports.manager.util.IdGenerator;
import java.util.Optional;

public class SqlServerTeamRepository implements Repository<Team> {

    private static final String SELECT_ALL_SQL = """
            SELECT Id, Name, Tag, Game, CaptainName, ContactEmail, Region, LogoUrl, Status
            FROM dbo.Teams
            ORDER BY Name ASC
            """;

    private static final String SELECT_BY_ID_SQL = """
            SELECT Id, Name, Tag, Game, CaptainName, ContactEmail, Region, LogoUrl, Status
            FROM dbo.Teams
            WHERE Id = ?
            """;

    private static final String UPSERT_SQL = """
            IF EXISTS (SELECT 1 FROM dbo.Teams WHERE Id = ?)
            BEGIN
                UPDATE dbo.Teams
                SET Name = ?,
                    Tag = ?,
                    Game = ?,
                    CaptainName = ?,
                    ContactEmail = ?,
                    Region = ?,
                    LogoUrl = ?,
                    Status = ?,
                    UpdatedAt = SYSDATETIME()
                WHERE Id = ?
            END
            ELSE
            BEGIN
                INSERT INTO dbo.Teams
                    (Id, Name, Tag, Game, CaptainName, ContactEmail, Region, LogoUrl, Status, CreatedAt, UpdatedAt)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATETIME(), SYSDATETIME())
            END
            """;

    private final DatabaseConnection dbConnection;
    private final TransactionManager transactionManager;
    private final ObservableList<Team> cache = FXCollections.observableArrayList();

    public SqlServerTeamRepository(DatabaseConnection dbConnection, TransactionManager transactionManager) {
        this.dbConnection = dbConnection;
        this.transactionManager = transactionManager;
        reloadCache();
    }

    public final void reloadCache() {
        List<Team> fresh = queryAllFromDb();
        cache.setAll(fresh);
    }

    private List<Team> queryAllFromDb() {
        List<Team> list = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải danh sách đội tuyển từ SQL Server: "
                    + DatabaseConnection.translateSqlException(e), e);
        }
        return list;
    }

    @Override
    public ObservableList<Team> findAll() {
        reloadCache();
        return cache;
    }

    @Override
    public Optional<Team> findById(String id) {
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
            throw new IllegalStateException("Không thể tìm đội tuyển theo ID: "
                    + DatabaseConnection.translateSqlException(e), e);
        }
    }

    @Override
    public Team save(Team entity) {
        if (entity.getId() == null || entity.getId().isBlank()) {
            entity.setId(IdGenerator.next("TEAM"));
        }

        String game = (entity.getGameTitle() != null && !entity.getGameTitle().isBlank())
                ? entity.getGameTitle()
                : "League of Legends";
        String tag = (entity.getTag() != null && !entity.getTag().isBlank())
                ? entity.getTag()
                : entity.getName().substring(0, Math.min(entity.getName().length(), 4)).toUpperCase();
        String logoUrl = entity.getAccentColor();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPSERT_SQL)) {

            // IF EXISTS
            ps.setString(1, entity.getId());

            // UPDATE
            ps.setString(2, entity.getName());
            ps.setString(3, tag);
            ps.setString(4, game);
            setNullableString(ps, 5, entity.getCoach());
            setNullableString(ps, 6, entity.getContactEmail());
            setNullableString(ps, 7, entity.getRegion());
            setNullableString(ps, 8, logoUrl);
            ps.setString(9, "ACTIVE");
            ps.setString(10, entity.getId());

            // INSERT
            ps.setString(11, entity.getId());
            ps.setString(12, entity.getName());
            ps.setString(13, tag);
            ps.setString(14, game);
            setNullableString(ps, 15, entity.getCoach());
            setNullableString(ps, 16, entity.getContactEmail());
            setNullableString(ps, 17, entity.getRegion());
            setNullableString(ps, 18, logoUrl);
            ps.setString(19, "ACTIVE");

            ps.executeUpdate();
            reloadCache();
            return entity;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể lưu đội tuyển vào SQL Server: "
                    + DatabaseConnection.translateSqlException(e), e);
        }
    }

    @Override
    public boolean deleteById(String id) {
        if (id == null || id.isBlank()) {
            return false;
        }

        String unlinkMatchesA = "UPDATE dbo.Matches SET Team1Id = NULL WHERE Team1Id = ?";
        String unlinkMatchesB = "UPDATE dbo.Matches SET Team2Id = NULL WHERE Team2Id = ?";
        String unlinkMatchesW = "UPDATE dbo.Matches SET WinnerTeamId = NULL WHERE WinnerTeamId = ?";
        String unlinkPrize = "UPDATE dbo.PrizeAllocations SET TeamId = NULL WHERE TeamId = ?";
        String deleteRegs = "DELETE FROM dbo.Registrations WHERE TeamId = ?";
        String deleteSql = "DELETE FROM dbo.Teams WHERE Id = ?";

        try {
            boolean deleted = transactionManager.executeInTransaction(conn -> {
                try (PreparedStatement psA = conn.prepareStatement(unlinkMatchesA)) {
                    psA.setString(1, id);
                    psA.executeUpdate();
                } catch (SQLException ignored) {}

                try (PreparedStatement psB = conn.prepareStatement(unlinkMatchesB)) {
                    psB.setString(1, id);
                    psB.executeUpdate();
                } catch (SQLException ignored) {}

                try (PreparedStatement psW = conn.prepareStatement(unlinkMatchesW)) {
                    psW.setString(1, id);
                    psW.executeUpdate();
                } catch (SQLException ignored) {}

                try (PreparedStatement psP = conn.prepareStatement(unlinkPrize)) {
                    psP.setString(1, id);
                    psP.executeUpdate();
                } catch (SQLException ignored) {}

                try (PreparedStatement psR = conn.prepareStatement(deleteRegs)) {
                    psR.setString(1, id);
                    psR.executeUpdate();
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
            throw new IllegalStateException("Không thể xóa đội tuyển trên SQL Server: "
                    + DatabaseConnection.translateSqlException(e), e);
        }
    }

    @Override
    public void clear() {
        String unlinkMatchesA = "UPDATE dbo.Matches SET Team1Id = NULL, Team2Id = NULL, WinnerTeamId = NULL";
        String unlinkPrize = "UPDATE dbo.PrizeAllocations SET TeamId = NULL";
        String deleteRegs = "DELETE FROM dbo.Registrations";
        String deleteAllSql = "DELETE FROM dbo.Teams";

        try {
            transactionManager.executeVoidInTransaction(conn -> {
                try (PreparedStatement ps1 = conn.prepareStatement(unlinkMatchesA)) {
                    ps1.executeUpdate();
                } catch (SQLException ignored) {}
                try (PreparedStatement ps2 = conn.prepareStatement(unlinkPrize)) {
                    ps2.executeUpdate();
                } catch (SQLException ignored) {}
                try (PreparedStatement ps3 = conn.prepareStatement(deleteRegs)) {
                    ps3.executeUpdate();
                } catch (SQLException ignored) {}
                try (PreparedStatement ps4 = conn.prepareStatement(deleteAllSql)) {
                    ps4.executeUpdate();
                }
            });
            cache.clear();
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể xóa toàn bộ đội tuyển: "
                    + DatabaseConnection.translateSqlException(e), e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM dbo.Teams";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể đếm số lượng đội tuyển: "
                    + DatabaseConnection.translateSqlException(e), e);
        }
    }

    private Team mapRow(ResultSet rs) throws SQLException {
        Team t = new Team();
        t.setId(rs.getString("Id"));
        t.setName(rs.getString("Name"));
        t.setTag(rs.getString("Tag"));
        t.setGameTitle(rs.getString("Game"));
        t.setCoach(rs.getString("CaptainName"));
        t.setContactEmail(rs.getString("ContactEmail"));
        t.setRegion(rs.getString("Region"));
        String logo = rs.getString("LogoUrl");
        if (logo != null && logo.startsWith("#")) t.setAccentColor(logo);
        return t;
    }

    private static void setNullableString(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null || value.isBlank()) {
            ps.setNull(index, Types.NVARCHAR);
        } else {
            ps.setString(index, value.trim());
        }
    }
}
