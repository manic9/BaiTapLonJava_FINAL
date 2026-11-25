package com.esports.manager.service;

import com.esports.manager.config.DatabaseConnection;
import com.esports.manager.config.PasswordHasher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class AuthenticationService {
    private static final String FIND_USER = "SELECT PasswordHash, PasswordSalt, IsActive FROM dbo.Users WHERE Username = ?";
    private final DatabaseConnection database;

    public AuthenticationService(DatabaseConnection database) {
        this.database = database;
    }

    public boolean authenticate(String username, char[] password) {
        if (username == null || username.isBlank() || password == null || password.length == 0) return false;
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_USER)) {
            statement.setString(1, username.trim());
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next() || !result.getBoolean("IsActive")) return false;
                return PasswordHasher.verify(password, result.getString("PasswordHash"), result.getString("PasswordSalt"));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể xác thực tài khoản với SQL Server: " + DatabaseConnection.translateSqlException(e), e);
        }
    }
}
