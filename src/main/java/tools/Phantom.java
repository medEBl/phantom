package tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Phantom {

    private final String url = "jdbc:mysql://localhost:3306/phantom";
    private final String user = "root";
    private final String password = "";

    private static Phantom instance;

    private Phantom() {}

    public static Phantom getInstance() {
        if (instance == null) {
            instance = new Phantom();
        }
        return instance;
    }

    // NEW CONNECTION EACH TIME (IMPORTANT FIX)
    public Connection getCnx() {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new RuntimeException("Database connection failed", e);
        }
    }
}