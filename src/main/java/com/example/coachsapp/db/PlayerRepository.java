package com.example.coachsapp.db;

import com.example.coachsapp.model.Player;
import com.example.coachsapp.model.Position;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for Player operations
 * Handles all database operations for players
 */
public class PlayerRepository {

    private Connection connection;

    public PlayerRepository() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Save a new player to database
     */
    public Player save(Player player) {
        if (player.getName() == null || player.getName().isEmpty()) {
            System.err.println("✗ Player name cannot be null or empty");
            return null;
        }

        String sql = "INSERT INTO player (name, age, jersey_number, position, injured, club_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, player.getName());
            pstmt.setInt(2, player.getAge());
            pstmt.setInt(3, player.getJersey());
            pstmt.setString(4, player.getPosition().toString());
            pstmt.setBoolean(5, player.isInjured());
            pstmt.setObject(6, player.getClubId(), Types.INTEGER);

            pstmt.executeUpdate();

            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                player.setId(generatedKeys.getInt(1));
                System.out.println("✓ Player saved: " + player.getName() + " - #" + player.getJersey() +
                                 " (" + player.getPosition() + ") (ID: " + player.getId() + ")");
                return player;
            }
        } catch (SQLException e) {
            System.err.println("✗ Error saving player: " + e.getMessage());
        }
        return null;
    }

    /**
     * Find player by ID
     */
    public Player findById(int id) {
        String sql = "SELECT id, name, age, jersey_number, position, injured, club_id FROM player WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToPlayer(rs);
            }
        } catch (SQLException e) {
            System.err.println("✗ Error finding player: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get all players
     */
    public List<Player> findAll() {
        List<Player> players = new ArrayList<>();
        String sql = "SELECT id, name, age, jersey_number, position, injured, club_id FROM player ORDER BY name";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                players.add(mapResultSetToPlayer(rs));
            }
        } catch (SQLException e) {
            System.err.println("✗ Error retrieving players: " + e.getMessage());
        }
        return players;
    }

    /**
     * Get all players by club ID
     */
    public List<Player> findByClubId(int clubId) {
        List<Player> players = new ArrayList<>();
        String sql = "SELECT id, name, age, jersey_number, position, injured, club_id FROM player " +
                     "WHERE club_id = ? ORDER BY name";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, clubId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                players.add(mapResultSetToPlayer(rs));
            }
        } catch (SQLException e) {
            System.err.println("✗ Error retrieving players by club: " + e.getMessage());
        }
        return players;
    }

    /**
     * Update player
     */
    public boolean update(Player player) {
        String sql = "UPDATE player SET name = ?, age = ?, jersey_number = ?, position = ?, injured = ?, " +
                     "club_id = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, player.getName());
            pstmt.setInt(2, player.getAge());
            pstmt.setInt(3, player.getJersey());
            pstmt.setString(4, player.getPosition().toString());
            pstmt.setBoolean(5, player.isInjured());
            pstmt.setObject(6, player.getClubId(), Types.INTEGER);
            pstmt.setInt(7, player.getId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("✓ Player updated: " + player.getName());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("✗ Error updating player: " + e.getMessage());
        }
        return false;
    }

    /**
     * Delete player by ID
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM player WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("✓ Player deleted (ID: " + id + ")");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("✗ Error deleting player: " + e.getMessage());
        }
        return false;
    }

    /**
     * Get total number of players
     */
    public int count() {
        String sql = "SELECT COUNT(*) FROM player";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("✗ Error counting players: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Helper method to map ResultSet to Player object
     */
    private Player mapResultSetToPlayer(ResultSet rs) throws SQLException {
        Player player = new Player(
            rs.getString("name"),
            rs.getInt("age"),
            rs.getInt("jersey_number"),
            Position.valueOf(rs.getString("position")),
            rs.getBoolean("injured")
        );
        player.setId(rs.getInt("id"));
        int clubId = rs.getInt("club_id");
        if (rs.wasNull()) {
            player.setClubId(null);
        } else {
            player.setClubId(clubId);
        }
        return player;
    }
}

