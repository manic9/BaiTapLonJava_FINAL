package com.esports.manager.config;

import com.esports.manager.model.AppSettings;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

/** Persists user-facing application preferences in dbo.AppSettings. */
public final class SqlServerAppSettingsStore {
    private static final Map<String, String> DEFAULTS = Map.of(
            "organizationName", "Nexus Arena Esports",
            "currencySymbol", "$",
            "defaultBestOf", "3",
            "autoAdvanceBracket", "true",
            "confirmBeforeDelete", "true",
            "language", "vi"
    );

    private final DatabaseConnection databaseConnection;
    private final TransactionManager transactionManager;

    public SqlServerAppSettingsStore(DatabaseConnection databaseConnection, TransactionManager transactionManager) {
        this.databaseConnection = databaseConnection;
        this.transactionManager = transactionManager;
    }

    public void loadInto(AppSettings settings) {
        Map<String, String> values = new java.util.HashMap<>(DEFAULTS);
        String sql = "SELECT SettingKey, SettingValue FROM dbo.AppSettings";
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) values.put(result.getString("SettingKey"), result.getString("SettingValue"));
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải cài đặt từ SQL Server: "
                    + DatabaseConnection.translateSqlException(e), e);
        }

        settings.setOrganizationName(values.get("organizationName"));
        settings.setCurrencySymbol(values.get("currencySymbol"));
        settings.setDefaultBestOf(parseInt(values.get("defaultBestOf"), 3));
        settings.setAutoAdvanceBracket(Boolean.parseBoolean(values.get("autoAdvanceBracket")));
        settings.setConfirmBeforeDelete(Boolean.parseBoolean(values.get("confirmBeforeDelete")));
        settings.setLanguage(values.get("language"));
    }

    public void save(AppSettings settings) {
        Map<String, String> values = Map.of(
                "organizationName", settings.getOrganizationName(),
                "currencySymbol", settings.getCurrencySymbol(),
                "defaultBestOf", Integer.toString(settings.getDefaultBestOf()),
                "autoAdvanceBracket", Boolean.toString(settings.isAutoAdvanceBracket()),
                "confirmBeforeDelete", Boolean.toString(settings.isConfirmBeforeDelete()),
                "language", settings.getLanguage()
        );
        String sql = """
                UPDATE dbo.AppSettings SET SettingValue = ?, UpdatedAt = SYSDATETIME()
                WHERE SettingKey = ?;
                IF @@ROWCOUNT = 0
                    INSERT INTO dbo.AppSettings (SettingKey, SettingValue, UpdatedAt) VALUES (?, ?, SYSDATETIME())
                """;
        try {
            transactionManager.executeVoidInTransaction(connection -> {
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    for (Map.Entry<String, String> entry : values.entrySet()) {
                        statement.setString(1, entry.getValue());
                        statement.setString(2, entry.getKey());
                        statement.setString(3, entry.getKey());
                        statement.setString(4, entry.getValue());
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }
            });
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể lưu cài đặt vào SQL Server: "
                    + DatabaseConnection.translateSqlException(e), e);
        }
    }

    private static int parseInt(String value, int fallback) {
        try { return Integer.parseInt(value); }
        catch (RuntimeException ignored) { return fallback; }
    }
}
