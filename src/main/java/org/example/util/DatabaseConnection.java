package org.example.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private static final String DB_URL = "jdbc:sqlite:code_evaluator.db?journal_mode=WAL&busy_timeout=5000";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        String createUserTableSQL = "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "first_name TEXT NOT NULL DEFAULT '', " +
                "last_name TEXT NOT NULL DEFAULT '', " +
                "email TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "role TEXT NOT NULL DEFAULT 'USER'" +
                ");";

        String createCategoryTableSQL = "CREATE TABLE IF NOT EXISTS category (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL DEFAULT '', " +
                "slug TEXT UNIQUE NOT NULL DEFAULT '', " +
                "description TEXT" +
                ");";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createUserTableSQL);
            stmt.execute(createCategoryTableSQL);

            seedDefaultUsers(conn);
            seedDefaultCategories(conn);

            System.out.println("Database SQLite initialized successfully with default seeds!");

        } catch (SQLException e) {
            System.err.println("Error initializing SQLite Database!");
            e.printStackTrace();
        }
    }

    private static void seedDefaultUsers(Connection conn) {
        String checkUserSQL = "SELECT COUNT(*) FROM users WHERE email = ?";
        String insertUserSQL = "INSERT INTO users (first_name, last_name, email, password, role) VALUES (?, ?, ?, ?, ?)";

        String[][] defaultUsers = {
                {"System", "Admin", "admin@codeevaluator.ai", "admin123", "ADMIN"},
                {"Test", "User", "user@codeevaluator.ai", "user123", "USER"}
        };

        try {
            for (String[] userData : defaultUsers) {
                try (PreparedStatement checkStmt = conn.prepareStatement(checkUserSQL)) {
                    checkStmt.setString(1, userData[2]);

                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next() && rs.getInt(1) == 0) {
                            try (PreparedStatement insertStmt = conn.prepareStatement(insertUserSQL)) {
                                insertStmt.setString(1, userData[0]);
                                insertStmt.setString(2, userData[1]);
                                insertStmt.setString(3, userData[2]);

                                String plainPassword = userData[3];
                                String hashedPassword = BCrypt.withDefaults().hashToString(12, plainPassword.toCharArray());

                                insertStmt.setString(4, hashedPassword);
                                insertStmt.setString(5, userData[4]);
                                insertStmt.executeUpdate();
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error seeding default users!");
            e.printStackTrace();
        }
    }

    private static void seedDefaultCategories(Connection conn) {
        String checkCategorySQL = "SELECT COUNT(*) FROM category WHERE slug = ?";
        String insertCategorySQL = "INSERT INTO category (name, slug, description) VALUES (?, ?, ?)";

        // 5 Default Categories
        String[][] defaultCategories = {
                {"Algorithms & Data Structures", "algorithms-ds", "Problems related to arrays, trees, graphs, and dynamic programming."},
                {"Object-Oriented Programming", "oop", "Core concepts including inheritance, polymorphism, and encapsulation."},
                {"Database Systems", "database-systems", "SQL queries, schema design, normalization, and indexing problems."},
                {"Web Development", "web-dev", "Frontend and backend web technologies including REST APIs and HTTP protocols."},
                {"System Design", "system-design", "Scalability, microservices architecture, caching, and load balancing concepts."}
        };

        try {
            for (String[] catData : defaultCategories) {
                try (PreparedStatement checkStmt = conn.prepareStatement(checkCategorySQL)) {
                    checkStmt.setString(1, catData[1]);

                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next() && rs.getInt(1) == 0) {
                            try (PreparedStatement insertStmt = conn.prepareStatement(insertCategorySQL)) {
                                insertStmt.setString(1, catData[0]);
                                insertStmt.setString(2, catData[1]);
                                insertStmt.setString(3, catData[2]);
                                insertStmt.executeUpdate();
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error seeding default categories!");
            e.printStackTrace();
        }
    }
}