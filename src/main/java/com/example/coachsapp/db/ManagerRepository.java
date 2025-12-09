package com.example.coachsapp.db;

import com.example.coachsapp.model.Manager;
import com.example.coachsapp.model.Club;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for Manager operations
 * Handles all database operations for managers
 */
public class ManagerRepository {

    private Connection connection;
    private ClubRepository clubRepository;

    public ManagerRepository() {
        this.connection = DatabaseConnection.getInstance().getConnection();
        this.clubRepository = new ClubRepository();
    }

    /**
     * Save a new manager to database
     */
    public Manager save(Manager manager) {
        if (manager.getName() == null || manager.getName().isEmpty()) {
            System.err.println("✗ Manager name cannot be null or empty");
            return null;
        }

        // First save the club if it doesn't exist
        Club club = manager.getClub();
        Club existingClub = clubRepository.findByName(club.getClubName());

        if (existingClub == null) {
            clubRepository.save(club);
        } else {
            club.setId(existingClub.getId());
        }

        String sql = "INSERT INTO manager (name, club_id) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, manager.getName());
            pstmt.setInt(2, club.getId());
            pstmt.executeUpdate();

            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                manager.setId(generatedKeys.getInt(1));
                System.out.println("✓ Manager saved: " + manager.getName() + " @ " + club.getClubName() + " (ID: " + manager.getId() + ")");
                return manager;
            }
        } catch (SQLException e) {
            System.err.println("✗ Error saving manager: " + e.getMessage());
        }
        return null;
    }

    /**
     * Find manager by ID
     */
    public Manager findById(int id) {
        String sql = "SELECT m.id, m.name, m.club_id, c.name as club_name FROM manager m " +
                     "JOIN club c ON m.club_id = c.id WHERE m.id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Club club = new Club(rs.getString("club_name"));
                club.setId(rs.getInt("club_id"));
                Manager manager = new Manager(rs.getString("name"), club);
                manager.setId(rs.getInt("id"));
                return manager;
            }
        } catch (SQLException e) {
            System.err.println("✗ Error finding manager: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get all managers
     */
    public List<Manager> findAll() {
        List<Manager> managers = new ArrayList<>();
        String sql = "SELECT m.id, m.name, m.club_id, c.name as club_name FROM manager m " +
                     "JOIN club c ON m.club_id = c.id ORDER BY m.name";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Club club = new Club(rs.getString("club_name"));
                club.setId(rs.getInt("club_id"));
                Manager manager = new Manager(rs.getString("name"), club);
                manager.setId(rs.getInt("id"));
                managers.add(manager);
            }
        } catch (SQLException e) {
            System.err.println("✗ Error retrieving managers: " + e.getMessage());
        }
        return managers;
    }

    /**
     * Update manager
     */
    public boolean update(Manager manager) {
        String sql = "UPDATE manager SET name = ?, club_id = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, manager.getName());
            pstmt.setInt(2, manager.getClub().getId());
            pstmt.setInt(3, manager.getId());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("✓ Manager updated: " + manager.getName());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("✗ Error updating manager: " + e.getMessage());
        }
        return false;
    }

    /**
     * Delete manager by ID
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM manager WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("✓ Manager deleted (ID: " + id + ")");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("✗ Error deleting manager: " + e.getMessage());
        }
        return false;
    }

    /**
     * Get total number of managers
     */
    public int count() {
        String sql = "SELECT COUNT(*) FROM manager";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("✗ Error counting managers: " + e.getMessage());
        }
        return 0;
    }
}

