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

    private static final String[][] defaultUsers = {{"System", "Admin", "admin@codeevaluator.ai", "admin123", "ADMIN"}, {"Test", "User", "user@codeevaluator.ai", "user123", "USER"}};

    private static final String[][] defaultCategories = {{"Algorithms & Data Structures", "algorithms-ds", "Problems related to arrays, trees, graphs, and dynamic programming."}, {"Object-Oriented Programming", "oop", "Core concepts including inheritance, polymorphism, and encapsulation."}, {"Database Systems", "database-systems", "SQL queries, schema design, normalization, and indexing problems."}, {"Web Development", "web-dev", "Frontend and backend web technologies including REST APIs and HTTP protocols."}, {"System Design", "system-design", "Scalability, microservices architecture, caching, and load balancing concepts."}};

    private static final String[][] defaultProblems = {
            {"Two Sum", "algorithms-ds", "EASY",
                    "<p>Given an array of integers, print the indices of two numbers that add up to a specific target.</p><p><b>Input Format:</b></p><ul><li>Line 1: An integer N (size of array).</li><li>Line 2: N space-separated integers.</li><li>Line 3: An integer Target.</li></ul><p><b>Output Format:</b></p><ul><li>Print two space-separated integers representing the indices.</li></ul>",
                    "4\n2 7 11 15\n9", "0 1"},

            {"Reverse a String", "algorithms-ds", "EASY",
                    "<p>Write a program that takes a string and prints it in reverse order.</p><p><b>Input Format:</b></p><ul><li>A single line containing a string.</li></ul><p><b>Output Format:</b></p><ul><li>A single line containing the reversed string.</li></ul>",
                    "hello", "olleh"},

            {"Merge Sort Implementation", "algorithms-ds", "MEDIUM",
                    "<p>Implement the merge sort algorithm to sort an array of integers.</p><p><b>Input Format:</b></p><ul><li>Line 1: An integer N representing the array size.</li><li>Line 2: N space-separated integers.</li></ul><p><b>Output Format:</b></p><ul><li>A single line of N space-separated integers in ascending order.</li></ul>",
                    "5\n3 1 4 5 2", "1 2 3 4 5"},

            {"Graph BFS Traversal", "algorithms-ds", "HARD",
                    "<p>Implement Breadth-First Search for an unweighted graph.</p><p><b>Input Format:</b></p><ul><li>Line 1: Two integers V (vertices) and E (edges).</li><li>Next E lines: Two integers representing an edge between nodes.</li></ul><p><b>Output Format:</b></p><ul><li>A single line of space-separated nodes in BFS traversal order starting from node 0.</li></ul>",
                    "4 3\n0 1\n1 2\n2 3", "0 1 2 3"},

            // Object-Oriented Programming
            {"Bank Account Simulator", "oop", "EASY",
                    "<p>Design a BankAccount class that supports deposit and withdraw operations.</p><p><b>Input Format:</b></p><ul><li>Line 1: Initial balance (integer).</li><li>Subsequent lines: Commands in format 'deposit X' or 'withdraw Y'.</li></ul><p><b>Output Format:</b></p><ul><li>Print the final balance.</li></ul>",
                    "100\ndeposit 50\nwithdraw 20", "130"},

            {"Shape Area Calculator", "oop", "EASY",
                    "<p>Implement classes for Circle and Rectangle. Compute area based on input dimensions.</p><p><b>Input Format:</b></p><ul><li>A single line containing the shape name and its dimension (e.g., 'circle R' or 'rectangle L W').</li></ul><p><b>Output Format:</b></p><ul><li>The calculated area formatted to two decimal places.</li></ul>",
                    "circle 5", "78.53"},

            {"Employee Hierarchy", "oop", "MEDIUM",
                    "<p>Calculate total salary of a manager including all subordinates.</p><p><b>Input Format:</b></p><ul><li>Line 1: Integer N (number of employees).</li><li>Next N lines: ID, Role, and Salary space-separated.</li></ul><p><b>Output Format:</b></p><ul><li>A single integer representing the total hierarchical salary.</li></ul>",
                    "3\n1 Manager 5000\n2 Dev 3000\n3 Dev 3000", "11000"},

            {"Parking Lot Allocation", "oop", "HARD",
                    "<p>Simulate a parking lot allocating spots for different vehicle types.</p><p><b>Input Format:</b></p><ul><li>Multiple lines, each containing a vehicle type ('car', 'bike', 'bus').</li></ul><p><b>Output Format:</b></p><ul><li>Print the allocated spot ID or 'rejected' for each vehicle.</li></ul>",
                    "car\nbike\nbus", "spot1\nspot2\nrejected"},

            // Database Systems
            {"Basic Data Filter", "database-systems", "EASY",
                    "<p>Filter students who scored more than 80 marks from the given text list.</p><p><b>Input Format:</b></p><ul><li>Multiple lines containing 'Name Score' separated by a space.</li></ul><p><b>Output Format:</b></p><ul><li>Print the names of students who scored > 80, each on a new line.</li></ul>",
                    "Ali 85\nSara 75", "Ali"},

            {"Mock SQL SELECT", "database-systems", "EASY",
                    "<p>Parse a mock SELECT statement and extract the requested column from CSV data.</p><p><b>Input Format:</b></p><ul><li>Line 1: A SELECT query (e.g., 'SELECT age').</li><li>Subsequent lines: CSV data with headers on the first data line.</li></ul><p><b>Output Format:</b></p><ul><li>The extracted column values, each on a new line.</li></ul>",
                    "SELECT age\nJohn,25\nJane,30", "25\n30"},

            {"Inner Join Simulation", "database-systems", "MEDIUM",
                    "<p>Simulate an inner join between two sets of key-value pairs based on IDs.</p><p><b>Input Format:</b></p><ul><li>Table 1 data (ID Value) line by line.</li><li>A separator line '---'.</li><li>Table 2 data (ID Value) line by line.</li></ul><p><b>Output Format:</b></p><ul><li>Joined rows in format 'ID Value1 Value2'.</li></ul>",
                    "1 A\n2 B\n---\n1 X\n3 Y", "1 A X"},

            {"Group By Count", "database-systems", "HARD",
                    "<p>Read a list of departments and output the count of employees in each.</p><p><b>Input Format:</b></p><ul><li>Multiple lines, each containing a department name.</li></ul><p><b>Output Format:</b></p><ul><li>Print 'Department Count' for each unique department.</li></ul>",
                    "HR\nIT\nHR\nIT\nIT", "HR 2\nIT 3"},

            // Web Development
            {"URL Parameter Parser", "web-dev", "EASY",
                    "<p>Extract query parameters from a given URL string.</p><p><b>Input Format:</b></p><ul><li>A single URL string containing query parameters.</li></ul><p><b>Output Format:</b></p><ul><li>Print each parameter as 'key:value' on a new line.</li></ul>",
                    "http://example.com?id=5&name=ali", "id:5\nname:ali"},

            {"HTML Tag Balancer", "web-dev", "MEDIUM",
                    "<p>Check if the HTML tags in the given string are properly closed.</p><p><b>Input Format:</b></p><ul><li>A single line containing an HTML string.</li></ul><p><b>Output Format:</b></p><ul><li>Print 'balanced' if tags match, otherwise 'unbalanced'.</li></ul>",
                    "<div><p></p></div>", "balanced"},

            {"Simple JSON Validator", "web-dev", "MEDIUM",
                    "<p>Check if a given string is a valid simple JSON object.</p><p><b>Input Format:</b></p><ul><li>A single line containing a JSON-like string.</li></ul><p><b>Output Format:</b></p><ul><li>Print 'valid' or 'invalid'.</li></ul>",
                    "{\"key\": \"value\"}", "valid"},

            {"REST Route Mapper", "web-dev", "HARD",
                    "<p>Map incoming HTTP requests to their corresponding controller names based on a route list.</p><p><b>Input Format:</b></p><ul><li>A single line containing the HTTP method and path (e.g., 'GET /users/123').</li></ul><p><b>Output Format:</b></p><ul><li>Print the mapped Controller name.</li></ul>",
                    "GET /users/123", "UserController"},

            // System Design
            {"Round Robin Load Balancer", "system-design", "EASY",
                    "<p>Implement a simple round-robin load balancer algorithm for 3 servers.</p><p><b>Input Format:</b></p><ul><li>A string specifying the number of requests (e.g., '4 requests').</li></ul><p><b>Output Format:</b></p><ul><li>Print the server sequence space-separated (e.g., 's1 s2 s3 s1').</li></ul>",
                    "4 requests", "s1 s2 s3 s1"},

            {"LRU Cache Eviction", "system-design", "MEDIUM",
                    "<p>Simulate an LRU cache of size 2. Print cache state after operations.</p><p><b>Input Format:</b></p><ul><li>Multiple lines containing cache operations ('put X' or 'get X').</li></ul><p><b>Output Format:</b></p><ul><li>Print the remaining items in the cache.</li></ul>",
                    "put 1\nput 2\nget 1\nput 3", "1 3"},

            {"Token Bucket Rate Limiter", "system-design", "HARD",
                    "<p>Determine if a request is allowed based on a 3-requests-per-second limit.</p><p><b>Input Format:</b></p><ul><li>Multiple lines representing request timestamps in seconds.</li></ul><p><b>Output Format:</b></p><ul><li>Print 'allow' or 'deny' for each request.</li></ul>",
                    "0.1\n0.2\n0.3\n0.4", "allow allow allow deny"},

            {"Consistent Hashing Ring", "system-design", "HARD",
                    "<p>Simulate a consistent hashing ring assigning keys to nodes.</p><p><b>Input Format:</b></p><ul><li>Line 1: Node configuration (e.g., '3 nodes').</li><li>Line 2: Space-separated keys.</li></ul><p><b>Output Format:</b></p><ul><li>Space-separated mapped node names.</li></ul>",
                    "3 nodes\nkey1 key2", "node2 node1"}
    };

    private static final String[][] defaultTestCases = {
            // Algorithms & Data Structures
            {"Two Sum", "3\n3 2 4\n6", "1 2"}, {"Two Sum", "2\n3 3\n6", "0 1"}, {"Reverse a String", "world", "dlrow"}, {"Reverse a String", "java", "avaj"}, {"Merge Sort Implementation", "4\n10 7 8 9", "7 8 9 10"}, {"Graph BFS Traversal", "5 4\n0 1\n0 2\n1 3\n2 4", "0 1 2 3 4"},

            // Object-Oriented Programming
            {"Bank Account Simulator", "200\nwithdraw 50\ndeposit 10", "160"}, {"Shape Area Calculator", "rectangle 4 5", "20.00"}, {"Employee Hierarchy", "2\n1 Manager 6000\n2 Dev 4000", "10000"}, {"Parking Lot Allocation", "bus\nbus", "spot1\nrejected"},

            // Database Systems
            {"Basic Data Filter", "Ali 90\nSara 70\nZain 85", "Ali\nZain"}, {"Mock SQL SELECT", "SELECT name\nJohn,25\nJane,30", "John\nJane"}, {"Inner Join Simulation", "1 A\n---\n1 X", "1 A X"}, {"Group By Count", "HR\nHR\nHR", "HR 3"},

            // Web Development
            {"URL Parameter Parser", "http://test.com?q=java&sort=asc", "q:java\nsort:asc"}, {"HTML Tag Balancer", "<div><p></div>", "unbalanced"}, {"Simple JSON Validator", "{key: value}", "invalid"}, {"REST Route Mapper", "POST /users", "UserController"},

            // System Design
            {"Round Robin Load Balancer", "2 requests", "s1 s2"}, {"LRU Cache Eviction", "put 1\nget 1\nput 2\nput 3", "2 3"}, {"Token Bucket Rate Limiter", "0.1\n0.1\n0.1\n0.1", "allow allow allow deny"}, {"Consistent Hashing Ring", "2 nodes\nkey1", "node1"}};

    public static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(DB_URL);

        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
            stmt.execute("PRAGMA busy_timeout = 5000;");
        }

        return connection;
    }

    public static void initializeDatabase() {
        String createUserTableSQL = "CREATE TABLE IF NOT EXISTS users (" + "id INTEGER PRIMARY KEY AUTOINCREMENT, " + "first_name TEXT NOT NULL DEFAULT '', " + "last_name TEXT NOT NULL DEFAULT '', " + "email TEXT UNIQUE NOT NULL, " + "password TEXT NOT NULL, " + "role TEXT NOT NULL DEFAULT 'USER', " + "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " + "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP" + ");";

        String createCategoryTableSQL =
                "CREATE TABLE IF NOT EXISTS category (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL DEFAULT '', " +
                        "slug TEXT UNIQUE NOT NULL DEFAULT '', " +
                        "description TEXT, " +
                        "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                        ");";

        String createProblemTableSQL =
                "CREATE TABLE IF NOT EXISTS problems (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "title TEXT NOT NULL DEFAULT '', " +
                        "description TEXT NOT NULL DEFAULT '', " +
                        "sample_input TEXT, " + "sample_output TEXT, " +
                        "category_id INTEGER, " +
                        "difficulty VARCHAR(20) CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD')) NOT NULL, " +
                        "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " + "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY(category_id) REFERENCES category(id) ON DELETE CASCADE" + ");";

        String createTestCaseTableSQL =
                "CREATE TABLE IF NOT EXISTS test_cases (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "input_data TEXT NOT NULL, " +
                        "expected_output TEXT NOT NULL, " +
                        "problem_id INTEGER NOT NULL, " +
                        "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY(problem_id) REFERENCES problems(id) ON DELETE CASCADE" +
                        ");";

        String createSubmissionTableSQL =
                "CREATE TABLE IF NOT EXISTS submissions (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "user_id INTEGER NOT NULL, " +
                        "problem_id INTEGER NOT NULL, " +
                        "language VARCHAR(20) CHECK (language IN ('JAVA', 'C++')) NOT NULL, " +
                        "code_content TEXT NOT NULL, " +
                        "verdict VARCHAR(30) CHECK (verdict IN ('PASSED', 'WRONG_ANSWER', 'COMPILATION_ERROR', 'TIME_LIMIT_EXCEEDED')) NOT NULL, " +
                        "submitted_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE, " +
                        "FOREIGN KEY(problem_id) REFERENCES problems(id) ON DELETE CASCADE" +
                        ");";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {

            stmt.execute(createUserTableSQL);
            stmt.execute(createCategoryTableSQL);
            stmt.execute(createProblemTableSQL);
            stmt.execute(createTestCaseTableSQL);
            stmt.execute(createSubmissionTableSQL);

            seedDefaultUsers(conn);
            seedDefaultCategories(conn);
            seedDefaultProblems(conn);
            seedDefaultTestCases(conn);

            System.out.println("Database SQLite initialized successfully with default seeds (Users, Categories, Problems, Test Cases)!");
        } catch (SQLException e) {
            System.err.println("Error initializing SQLite Database!");
            e.printStackTrace();
        }
    }

    private static void seedDefaultUsers(Connection conn) {
        String checkUserSQL = "SELECT COUNT(*) FROM users WHERE email = ?";
        String insertUserSQL = "INSERT INTO users (first_name, last_name, email, password, role) VALUES (?, ?, ?, ?, ?)";

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

    private static void seedDefaultProblems(Connection conn) {
        String getCategoryIdSQL = "SELECT id FROM category WHERE slug = ?";
        String checkProblemSQL = "SELECT COUNT(*) FROM problems WHERE title = ?";
        String insertProblemSQL = "INSERT INTO problems (title , category_id , difficulty , description , sample_input , sample_output) VALUES (? , ? ,? , ? , ? , ?)";

        try {
            for (String[] probData : defaultProblems) {
                int categoryId = -1;

                try (PreparedStatement catStmt = conn.prepareStatement(getCategoryIdSQL)) {
                    catStmt.setString(1, probData[1]);

                    try (ResultSet rs = catStmt.executeQuery()) {
                        if (rs.next()) {
                            categoryId = rs.getInt("id");
                        }
                    }
                }

                if (categoryId == -1) continue;

                try (PreparedStatement checkStmt = conn.prepareStatement(checkProblemSQL)) {
                    checkStmt.setString(1, probData[0]);

                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next() && rs.getInt(1) == 0) {
                            try (PreparedStatement insertStmt = conn.prepareStatement(insertProblemSQL)) {
                                insertStmt.setString(1, probData[0]);
                                insertStmt.setInt(2, categoryId);
                                insertStmt.setString(3, probData[2]);
                                insertStmt.setString(4, probData[3]);
                                insertStmt.setString(5, probData[4]);
                                insertStmt.setString(6, probData[5]);
                                insertStmt.executeUpdate();
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error seeding default problems!");
            e.printStackTrace();
        }
    }

    private static void seedDefaultTestCases(Connection conn) {
        String getProblemIdSQL = "SELECT id FROM problems WHERE title = ?";
        String checkTestCaseSQL = "SELECT COUNT(*) FROM test_cases WHERE problem_id = ? AND input_data = ?";
        String insertTestCaseSQL = "INSERT INTO test_cases (problem_id, input_data, expected_output) VALUES (? , ? , ?)";

        try {
            for (String[] testData : defaultTestCases) {
                int problemId = -1;

                try (PreparedStatement probIdStmt = conn.prepareStatement(getProblemIdSQL)) {
                    probIdStmt.setString(1, testData[0]);
                    try (ResultSet rs = probIdStmt.executeQuery()) {
                        if (rs.next()) {
                            problemId = rs.getInt("id");
                        }
                    }
                }

                if (problemId == -1) continue;

                try (PreparedStatement checkStmt = conn.prepareStatement(checkTestCaseSQL)) {
                    checkStmt.setInt(1, problemId);
                    checkStmt.setString(2, testData[1]);

                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next() && rs.getInt(1) == 0) {

                            try (PreparedStatement insertStmt = conn.prepareStatement(insertTestCaseSQL)) {
                                insertStmt.setInt(1, problemId);
                                insertStmt.setString(2, testData[1]);
                                insertStmt.setString(3, testData[2]);
                                insertStmt.executeUpdate();
                            }

                        }
                    }
                }

            }
        } catch (SQLException e) {
            System.err.println("Error seeding default test cases!");
            e.printStackTrace();
        }
    }
}