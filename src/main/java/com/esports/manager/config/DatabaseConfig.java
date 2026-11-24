package com.esports.manager.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public final class DatabaseConfig {

    public enum AuthMode {
        SQL_SERVER,
        WINDOWS
    }

    private static final Path DEFAULT_CONFIG_PATH = Paths.get("config", "db.properties");

    private final String host;
    private final int port;
    private final String databaseName;
    private final AuthMode authMode;
    private final String username;
    private final String password;
    private final boolean encrypt;
    private final boolean trustServerCertificate;
    private final int loginTimeoutSeconds;
    private final boolean configFileFound;

    private DatabaseConfig(
            String host, int port, String databaseName, AuthMode authMode,
            String username, String password, boolean encrypt,
            boolean trustServerCertificate, int loginTimeoutSeconds, boolean configFileFound) {
        this.host = host;
        this.port = port;
        this.databaseName = databaseName;
        this.authMode = authMode;
        this.username = username;
        this.password = password;
        this.encrypt = encrypt;
        this.trustServerCertificate = trustServerCertificate;
        this.loginTimeoutSeconds = loginTimeoutSeconds;
        this.configFileFound = configFileFound;
    }

    public static DatabaseConfig load() {
        return load(DEFAULT_CONFIG_PATH);
    }

    public static DatabaseConfig load(Path configPath) {
        Properties props = new Properties();
        boolean fileExists = Files.exists(configPath);

        if (fileExists) {
            try (InputStream in = Files.newInputStream(configPath)) {
                props.load(in);
            } catch (IOException e) {
                throw new IllegalStateException("Không thể đọc file cấu hình database: " + configPath.toAbsolutePath(), e);
            }
        }

        String host = resolve("ESPORTS_DB_HOST", props.getProperty("db.host"), "localhost");
        int port = parseIntSafe(resolve("ESPORTS_DB_PORT", props.getProperty("db.port"), "1433"), 1433);
        String dbName = resolve("ESPORTS_DB_NAME", props.getProperty("db.name"), "EsportsDB");
        String authRaw = resolve("ESPORTS_DB_AUTH_MODE", props.getProperty("db.authMode"), "SQL_SERVER");
        AuthMode authMode = "WINDOWS".equalsIgnoreCase(authRaw.trim()) ? AuthMode.WINDOWS : AuthMode.SQL_SERVER;
        String username = resolve("ESPORTS_DB_USER", props.getProperty("db.username"), "esports_app");
        String password = resolve("ESPORTS_DB_PASSWORD", props.getProperty("db.password"), "");
        boolean encrypt = Boolean.parseBoolean(resolve("ESPORTS_DB_ENCRYPT", props.getProperty("db.encrypt"), "true"));
        boolean trustCert = Boolean.parseBoolean(resolve("ESPORTS_DB_TRUST_CERT", props.getProperty("db.trustServerCertificate"), "true"));
        int timeout = parseIntSafe(resolve("ESPORTS_DB_TIMEOUT", props.getProperty("db.loginTimeoutSeconds"), "5"), 5);

        return new DatabaseConfig(
                host.trim(), port, dbName.trim(), authMode,
                username.trim(), password, encrypt, trustCert, timeout, fileExists
        );
    }

    private static String resolve(String envKey, String propValue, String defaultValue) {
        String envVal = System.getenv(envKey);
        if (envVal != null && !envVal.isBlank()) return envVal;
        if (propValue != null && !propValue.isBlank()) return propValue;
        return defaultValue;
    }

    private static int parseIntSafe(String raw, int fallback) {
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public String buildJdbcUrl() {
        StringBuilder sb = new StringBuilder();
        sb.append("jdbc:sqlserver://")
                .append(host).append(":").append(port)
                .append(";databaseName=").append(databaseName)
                .append(";encrypt=").append(encrypt)
                .append(";trustServerCertificate=").append(trustServerCertificate)
                .append(";loginTimeout=").append(loginTimeoutSeconds)
                .append(";sendStringParametersAsUnicode=true;");
        if (authMode == AuthMode.WINDOWS) {
            sb.append("integratedSecurity=true;");
        }
        return sb.toString();
    }

    public String getHost() { return host; }
    public int getPort() { return port; }
    public String getDatabaseName() { return databaseName; }
    public AuthMode getAuthMode() { return authMode; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public boolean isConfigFileFound() { return configFileFound; }
}