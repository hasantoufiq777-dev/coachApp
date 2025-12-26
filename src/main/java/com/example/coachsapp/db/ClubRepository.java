package com.example.coachsapp.db;

import com.example.coachsapp.model.Club;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class ClubRepository {


    public Club save(Club club) {
        if (club.getClubName() == null || club.getClubName().isEmpty()) {
            System.err.println("✗ Club name cannot be null or empty");
            return null;
        }


        Club existing = findByName(club.getClubName());
        if (existing != null) {
            System.err.println("✗ Club already exists: " + club.getClubName() + " (ID: " + existing.getId() + ")");
            return null;
        }

        String sql = "INSERT INTO clubs (name) VALUES (?)";
        Connection connection = DatabaseConnection.getInstance().getConnection();
        
        if (connection == null) {
            System.err.println("✗ Database connection is null");
            return null;
        }
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, club.getClubName());
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                System.err.println("✗ No rows affected when saving club");
                return null;
            }

            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                club.setId(generatedKeys.getInt(1));
                System.out.println("✓ Club saved: " + club.getClubName() + " (ID: " + club.getId() + ")");
                return club;
            } else {
                System.err.println("✗ No generated key returned");
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                System.err.println("✗ Club name already exists: " + club.getClubName());
            } else {
                System.err.println("✗ Error saving club: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return null;
    }


    public Club findById(int id) {
        String sql = "SELECT id, name, created_at FROM clubs WHERE id = ?";
        Connection connection = DatabaseConnection.getInstance().getConnection();
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


    public Club findByName(String name) {
        String sql = "SELECT id, name, created_at FROM clubs WHERE name = ?";
        Connection connection = DatabaseConnection.getInstance().getConnection();
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


    public List<Club> findAll() {
        List<Club> clubs = new ArrayList<>();
        String sql = "SELECT id, name, created_at FROM clubs ORDER BY name";
        Connection connection = DatabaseConnection.getInstance().getConnection();
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


    public boolean update(Club club) {
        String sql = "UPDATE clubs SET name = ? WHERE id = ?";
        Connection connection = DatabaseConnection.getInstance().getConnection();
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


    public boolean delete(int id) {
        String sql = "DELETE FROM clubs WHERE id = ?";
        Connection connection = DatabaseConnection.getInstance().getConnection();
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


    public int count() {
        String sql = "SELECT COUNT(*) FROM clubs";
        Connection connection = DatabaseConnection.getInstance().getConnection();
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

