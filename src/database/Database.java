package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by alex on 16.04.15.
 */
public final class Database {

    private static Connection connection;
    private static final String DRIVER_NAME = "";
    private static final String URL = "jdbc:mysql://localhost/company";
    private static final String USER = "root";
    private static final String PASS= "root";

    private Database() {
    }

    private static void connect() throws SQLException {
        try {
            Class.forName(DRIVER_NAME).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        connection = DriverManager.getConnection(URL, USER, PASS);
    }

    public static Connection getConnection() throws SQLException, ClassNotFoundException{
        if(connection != null && !connection.isClosed()) {
            return connection;
        }
        connect();
        return connection;
    }
}
