package com.example.coachsapp.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Database connection manager and initialization
 * Handles SQLite database setup and schema creation
 */
public class DatabaseConnection {

    private static final String DATABASE_URL = "jdbc:sqlite:coaches_app.db";
    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection(DATABASE_URL);
            this.connection.setAutoCommit(true);
            System.out.println("✓ Database connected: " + DATABASE_URL);
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("✗ Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DATABASE_URL);
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("✗ Connection error: " + e.getMessage());
        }
        return connection;
    }

    public void initializeDatabase() {
        try (Statement statement = connection.createStatement()) {
            System.out.println("\n=== Initializing Database Schema ===");

            statement.execute("CREATE TABLE IF NOT EXISTS club (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            System.out.println("✓ Club table created/verified");

            statement.execute("CREATE TABLE IF NOT EXISTS manager (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, club_id INTEGER NOT NULL UNIQUE, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (club_id) REFERENCES club(id) ON DELETE CASCADE)");
            System.out.println("✓ Manager table created/verified");

            statement.execute("CREATE TABLE IF NOT EXISTS player (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, age INTEGER NOT NULL, jersey_number INTEGER NOT NULL, position TEXT NOT NULL, injured BOOLEAN DEFAULT 0, club_id INTEGER, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (club_id) REFERENCES club(id) ON DELETE SET NULL)");
            System.out.println("✓ Player table created/verified");

            statement.execute("CREATE TABLE IF NOT EXISTS transfer_history (id INTEGER PRIMARY KEY AUTOINCREMENT, player_id INTEGER NOT NULL, from_club_id INTEGER, to_club_id INTEGER, transfer_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (player_id) REFERENCES player(id), FOREIGN KEY (from_club_id) REFERENCES club(id), FOREIGN KEY (to_club_id) REFERENCES club(id))");
            System.out.println("✓ Transfer History table created/verified");

            System.out.println("✓ Database schema initialized successfully\n");
        } catch (SQLException e) {
            System.err.println("✗ Database initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✓ Database connection closed");
            }
        } catch (SQLException e) {
            System.err.println("✗ Error closing connection: " + e.getMessage());
        }
    }

    public void dropAllTables() {
        try (Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS transfer_history");
            statement.execute("DROP TABLE IF EXISTS player");
            statement.execute("DROP TABLE IF EXISTS manager");
            statement.execute("DROP TABLE IF EXISTS club");
            System.out.println("✓ All tables dropped");
        } catch (SQLException e) {
            System.err.println("✗ Error dropping tables: " + e.getMessage());
        }
    }
}

