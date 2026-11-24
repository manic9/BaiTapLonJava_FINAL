package com.esports.manager.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

public final class TransactionManager {

    @FunctionalInterface
    public interface SqlWork<T> {
        T execute(Connection connection) throws SQLException;
    }

    @FunctionalInterface
    public interface SqlVoidWork {
        void execute(Connection connection) throws SQLException;
    }

    private final DatabaseConnection databaseConnection;

    public TransactionManager(DatabaseConnection databaseConnection) {
        this.databaseConnection = Objects.requireNonNull(databaseConnection);
    }

    public <T> T executeInTransaction(SqlWork<T> work) throws SQLException {
        try (Connection conn = databaseConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                T result = work.execute(conn);
                conn.commit();
                return result;
            } catch (Throwable ex) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    ex.addSuppressed(rollbackEx);
                }
                if (ex instanceof SQLException sqlEx) throw sqlEx;
                if (ex instanceof RuntimeException runEx) throw runEx;
                throw new SQLException("Giao dịch thất bại: " + ex.getMessage(), ex);
            } finally {
                try {
                    conn.setAutoCommit(previousAutoCommit);
                } catch (SQLException ignored) {
                }
            }
        }
    }

    public void executeVoidInTransaction(SqlVoidWork work) throws SQLException {
        executeInTransaction(conn -> {
            work.execute(conn);
            return null;
        });
    }
}