package com.esports.manager.service;

import com.esports.manager.config.DatabaseConnection;
import com.esports.manager.config.PasswordHasher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public final class AuthenticationService {
    private static final String FIND_USER = "SELECT Username, FullName, Role, PasswordHash, PasswordSalt, IsActive FROM dbo.Users WHERE Username = ?";
    private final DatabaseConnection database;

    public AuthenticationService(DatabaseConnection database) {
        this.database = database;
    }

    public boolean authenticate(String username, char[] password) {
        return authenticateUser(username, password).isPresent();
    }

    public Optional<AuthenticatedUser> authenticateUser(String username, char[] password) {
        if (username == null || username.isBlank() || password == null || password.length == 0) return Optional.empty();
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_USER)) {
            statement.setString(1, username.trim());
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next() || !result.getBoolean("IsActive")) return Optional.empty();
                if (!PasswordHasher.verify(password, result.getString("PasswordHash"), result.getString("PasswordSalt"))) {
                    return Optional.empty();
                }
                String fullName = result.getString("FullName");
                if (fullName == null || fullName.isBlank()) fullName = result.getString("Username");
                return Optional.of(new AuthenticatedUser(
                        result.getString("Username"), fullName, result.getString("Role")));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể xác thực tài khoản với SQL Server: " + DatabaseConnection.translateSqlException(e), e);
        }
    }

    public record AuthenticatedUser(String username, String fullName, String role) {}

    public void register(String username, String fullName, char[] password) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Tên đăng nhập không được để trống.");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Họ tên không được để trống.");
        }
        if (fullName.trim().length() > 100
                || !username.trim().matches("[A-Za-z0-9._-]{3,50}")) {
            throw new IllegalArgumentException("Họ tên tối đa 100 ký tự; tên đăng nhập phải dài 3-50 ký tự và chỉ dùng chữ, số, dấu chấm, gạch dưới hoặc gạch ngang.");
        }
        if (password == null || password.length < 8) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 8 ký tự.");
        }
        if (password.length > 128) {
            throw new IllegalArgumentException("Mật khẩu không được vượt quá 128 ký tự.");
        }

        String normalizedUsername = username.trim();
        String duplicateSql = "SELECT 1 FROM dbo.Users WHERE Username = ?";
        String insertSql = """
                INSERT INTO dbo.Users (Id, Username, PasswordHash, PasswordSalt, FullName, Role, IsActive, CreatedAt)
                VALUES (?, ?, ?, ?, ?, N'ORGANIZER', 1, SYSDATETIME())
                """;

        try (Connection connection = database.getConnection()) {
            try (PreparedStatement check = connection.prepareStatement(duplicateSql)) {
                check.setString(1, normalizedUsername);
                try (ResultSet result = check.executeQuery()) {
                    if (result.next()) {
                        throw new IllegalArgumentException("Tên đăng nhập này đã được sử dụng.");
                    }
                }
            }

            PasswordHasher.HashedPassword hashed = PasswordHasher.hash(password);
            try (PreparedStatement insert = connection.prepareStatement(insertSql)) {
                insert.setString(1, UUID.randomUUID().toString());
                insert.setString(2, normalizedUsername);
                insert.setString(3, hashed.hashBase64());
                insert.setString(4, hashed.saltBase64());
                insert.setString(5, fullName.trim());
                insert.executeUpdate();
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 2601 || e.getErrorCode() == 2627) {
                throw new IllegalArgumentException("Tên đăng nhập này đã được sử dụng.");
            }
            throw new IllegalStateException("Không thể tạo tài khoản trong SQL Server: "
                    + DatabaseConnection.translateSqlException(e), e);
        } finally {
            if (password != null) java.util.Arrays.fill(password, '\0');
        }
    }
}
