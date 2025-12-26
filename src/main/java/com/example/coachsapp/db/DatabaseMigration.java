package com.example.coachsapp.db;

import java.sql.*;

/**
 * Database migration utility to update the schema
 */
public class DatabaseMigration {
    
    public static void migrateToVersion2() {
        System.out.println("=== Running Database Migration ===");
        Connection connection = DatabaseConnection.getInstance().getConnection();
        
        try {
            // Check if age column exists
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "managers", "age");
            
            if (!columns.next()) {
                // Age column doesn't exist, add it
                Statement statement = connection.createStatement();
                statement.execute("ALTER TABLE managers ADD COLUMN age INTEGER");
                System.out.println("✓ Added 'age' column to managers table");
            } else {
                System.out.println("✓ 'age' column already exists in managers table");
            }
            columns.close();
            
        } catch (SQLException e) {
            System.err.println("✗ Migration failed: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("=== Migration Complete ===\n");
    }
    
    public static void main(String[] args) {
        migrateToVersion2();
    }
}
