package com.esports.manager.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public final class DatabaseConnection {

    public record ConnectionStatus(boolean connected, String message, long latencyMs) {}

    private final DatabaseConfig config;

    public DatabaseConnection(DatabaseConfig config) {
        this.config = Objects.requireNonNull(config, "DatabaseConfig không được null");
        ensureDriverLoaded();
    }

    private void ensureDriverLoaded() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "Không tìm thấy Microsoft SQL Server JDBC Driver (mssql-jdbc). " +
                            "Hãy đặt file JAR vào thư mục lib/ và thêm vào classpath.", e);
        }
    }

    public Connection getConnection() throws SQLException {
        if (!config.isConfigFileFound()
                && config.getAuthMode() == DatabaseConfig.AuthMode.SQL_SERVER
                && (config.getPassword() == null || config.getPassword().isEmpty())) {
            throw new SQLException(
                    "Chưa tìm thấy file cấu hình config/db.properties. " +
                            "Hãy sao chép từ config/db.properties.example và điền mật khẩu SQL Server.");
        }

        String jdbcUrl = config.buildJdbcUrl();
        if (config.getAuthMode() == DatabaseConfig.AuthMode.WINDOWS) {
            return DriverManager.getConnection(jdbcUrl);
        }
        return DriverManager.getConnection(jdbcUrl, config.getUsername(), config.getPassword());
    }

    public ConnectionStatus testConnection() {
        long start = System.nanoTime();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT 1");
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                long ms = (System.nanoTime() - start) / 1_000_000L;
                return new ConnectionStatus(
                        true,
                        "Đã kết nối SQL Server (" + config.getHost() + "/" + config.getDatabaseName() + ")",
                        ms
                );
            }
            return new ConnectionStatus(false, "SQL Server không phản hồi truy vấn kiểm tra.", 0);
        } catch (SQLException e) {
            return new ConnectionStatus(false, translateSqlException(e), 0);
        } catch (UnsatisfiedLinkError e) {
            return new ConnectionStatus(
                    false,
                    "Thiếu thư viện mssql-jdbc_auth-x64.dll trong java.library.path cho chế độ Windows Authentication.",
                    0
            );
        }
    }

    public static String translateSqlException(SQLException e) {
        int code = e.getErrorCode();
        String msg = e.getMessage() != null ? e.getMessage() : "";

        if (code == 18456 || msg.contains("Login failed")) {
            return "Đăng nhập SQL Server thất bại. Kiểm tra lại db.username và db.password trong config/db.properties.";
        }
        if (code == 4060 || msg.contains("Cannot open database")) {
            return "Không tìm thấy cơ sở dữ liệu EsportsDB. Hãy chạy script tạo database trên SSMS trước.";
        }
        if (msg.contains("TCP/IP connection") || msg.contains("Connection refused") || msg.contains("timed out")) {
            return "Không thể kết nối tới máy chủ SQL Server. Hãy kiểm tra SQL Server Service đã chạy và bật TCP/IP cổng 1433.";
        }
        return "Lỗi SQL Server (Mã lỗi " + code + "): " + msg;
    }

    public DatabaseConfig getConfig() {
        return config;
    }
}