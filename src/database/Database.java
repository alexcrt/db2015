package database;

import oracle.jdbc.driver.OracleDriver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Created by alex on 16.04.15.
 */
public final class Database {

    private static Connection connection;
    private static final String DRIVER_NAME = "oracle.jdbc.OracleDriver";
    private static final String URL = "jdbc:oracle:thin:@diassrv2.epfl.ch:1521:orcldias";

    private Database() {
    }

    public static void connect(String username, String password) throws SQLException {
        if(connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, username, password);
        }
    }

    public static Connection getConnection() throws SQLException {
        checkConnection();
        return connection;
    }

    private static void checkConnection() throws SQLException {
        Objects.requireNonNull(connection);
        if(connection.isClosed()) {
            throw new SQLException("Connection closed");
        }
    }
}
