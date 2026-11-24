package com.nexusarena.exception;

import com.nexusarena.db.DatabaseConnection;
import java.sql.SQLException;

public class DataAccessException extends RuntimeException {

    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public static DataAccessException fromSqlException(String contextAction, SQLException e) {
        String friendly = DatabaseConnection.translateSqlException(e);
        return new DataAccessException(contextAction + ": " + friendly, e);
    }
}