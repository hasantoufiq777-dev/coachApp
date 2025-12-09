package com.example.coachsapp.db;

import com.example.coachsapp.model.Club;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for Club operations
 * Handles all database operations for clubs
 */
public class ClubRepository {

    private Connection connection;

    public ClubRepository() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Save a new club to database
     */
    public Club save(Club club) {
        if (club.getClubName() == null || club.getClubName().isEmpty()) {
            System.err.println("✗ Club name cannot be null or empty");
            return null;
        }

        String sql = "INSERT INTO club (name) VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, club.getClubName());
            pstmt.executeUpdate();

            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                club.setId(generatedKeys.getInt(1));
                System.out.println("✓ Club saved: " + club.getClubName() + " (ID: " + club.getId() + ")");
                return club;
            }
        } catch (SQLException e) {
            System.err.println("✗ Error saving club: " + e.getMessage());
        }
        return null;
    }

    /**
     * Find club by ID
     */
    public Club findById(int id) {
        String sql = "SELECT id, name, created_at FROM club WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Club club = new Club(rs.getString("name"));
                club.setId(rs.getInt("id"));
                return club;
            }
        } catch (SQLException e) {
            System.err.println("✗ Error finding club: " + e.getMessage());
        }
        return null;
    }

    /**
     * Find club by name
     */
    public Club findByName(String name) {
        String sql = "SELECT id, name, created_at FROM club WHERE name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Club club = new Club(rs.getString("name"));
                club.setId(rs.getInt("id"));
                return club;
            }
        } catch (SQLException e) {
            System.err.println("✗ Error finding club: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get all clubs
     */
    public List<Club> findAll() {
        List<Club> clubs = new ArrayList<>();
        String sql = "SELECT id, name, created_at FROM club ORDER BY name";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Club club = new Club(rs.getString("name"));
                club.setId(rs.getInt("id"));
                clubs.add(club);
            }
        } catch (SQLException e) {
            System.err.println("✗ Error retrieving clubs: " + e.getMessage());
        }
        return clubs;
    }

    /**
     * Update club
     */
    public boolean update(Club club) {
        String sql = "UPDATE club SET name = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, club.getClubName());
            pstmt.setInt(2, club.getId());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("✓ Club updated: " + club.getClubName());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("✗ Error updating club: " + e.getMessage());
        }
        return false;
    }

    /**
     * Delete club by ID
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM club WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("✓ Club deleted (ID: " + id + ")");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("✗ Error deleting club: " + e.getMessage());
        }
        return false;
    }

    /**
     * Get total number of clubs
     */
    public int count() {
        String sql = "SELECT COUNT(*) FROM club";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("✗ Error counting clubs: " + e.getMessage());
        }
        return 0;
    }
}

