package com.financeportal.dao;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * DBConnection helper.
 * Loads /database.properties from classpath if present; otherwise uses defaults.
 *
 * Expected keys (database.properties):
 *   db.url=jdbc:mysql://localhost:3306/finance_portal?serverTimezone=UTC
 *   db.user=root
 *   db.password=
 */
public final class DBConnection {
    private static final String RESOURCE = "/database.properties";
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/finance_portal?serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";

    private static final String url;
    private static final String user;
    private static final String password;

    static {
        String u = DEFAULT_URL;
        String usr = DEFAULT_USER;
        String pwd = DEFAULT_PASSWORD;
        try (InputStream in = DBConnection.class.getResourceAsStream(RESOURCE)) {
            if (in != null) {
                Properties p = new Properties();
                p.load(in);
                u = p.getProperty("db.url", DEFAULT_URL);
                usr = p.getProperty("db.user", DEFAULT_USER);
                pwd = p.getProperty("db.password", DEFAULT_PASSWORD);
            }
        } catch (Exception e) {
            System.err.println("Could not read " + RESOURCE + " - using defaults. " + e.getMessage());
        }
        url = u;
        user = usr;
        password = pwd;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found on classpath: " + e.getMessage());
        }
    }

    private DBConnection() {}

    /**
     * Get a new JDBC connection. Caller must close it.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
