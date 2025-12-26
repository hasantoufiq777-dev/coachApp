package com.example.coachsapp.db;

import com.example.coachsapp.model.Player;
import com.example.coachsapp.model.Position;
import com.example.coachsapp.model.Club;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for Player operations
 * Handles all database operations for players
 */
public class PlayerRepository {

    private ClubRepository clubRepository;

    public PlayerRepository() {
        this.clubRepository = new ClubRepository();
    }

    /**
     * Save a new player to database
     */
    public Player save(Player player) {
        Connection connection = DatabaseConnection.getInstance().getConnection();
        if (player.getName() == null || player.getName().isEmpty()) {
            System.err.println("✗ Player name cannot be null or empty");
            return null;
        }

        String sql = "INSERT INTO players (name, age, jersey_number, position, injured, club_id, club_view) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, player.getName());
            pstmt.setInt(2, player.getAge());
            pstmt.setInt(3, player.getJersey());
            pstmt.setString(4, player.getPosition().toString());
            pstmt.setBoolean(5, player.isInjured());
            pstmt.setObject(6, player.getClubId(), Types.INTEGER);

            // Determine club_view snapshot
            String clubView = null;
            if (player.getClubId() != null) {
                Club c = clubRepository.findById(player.getClubId());
                if (c != null) clubView = c.getClubName();
            }
            pstmt.setString(7, clubView);

            pstmt.executeUpdate();

            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                player.setId(generatedKeys.getInt(1));
                player.setClubView(clubView);
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
        Connection connection = DatabaseConnection.getInstance().getConnection();
        String sql = "SELECT id, name, age, jersey_number, position, injured, club_id, club_view FROM players WHERE id = ?";
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
        Connection connection = DatabaseConnection.getInstance().getConnection();
        List<Player> players = new ArrayList<>();
        String sql = "SELECT id, name, age, jersey_number, position, injured, club_id, club_view FROM players ORDER BY name";
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
        Connection connection = DatabaseConnection.getInstance().getConnection();
        List<Player> players = new ArrayList<>();
        String sql = "SELECT id, name, age, jersey_number, position, injured, club_id, club_view FROM players " +
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


    public boolean update(Player player) {
        Connection connection = DatabaseConnection.getInstance().getConnection();
        String sql = "UPDATE players SET name = ?, age = ?, jersey_number = ?, position = ?, injured = ?, " +
                     "club_id = ?, club_view = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, player.getName());
            pstmt.setInt(2, player.getAge());
            pstmt.setInt(3, player.getJersey());
            pstmt.setString(4, player.getPosition().toString());
            pstmt.setBoolean(5, player.isInjured());
            pstmt.setObject(6, player.getClubId(), Types.INTEGER);

          
            String clubView = null;
            if (player.getClubId() != null) {
                Club c = clubRepository.findById(player.getClubId());
                if (c != null) clubView = c.getClubName();
            }
            pstmt.setString(7, clubView);
            pstmt.setInt(8, player.getId());

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

 
    public boolean delete(int id) {
        Connection connection = DatabaseConnection.getInstance().getConnection();
        
        if (connection == null) {
            System.err.println("✗ Database connection is null when trying to delete player");
            return false;
        }
        
        try {
            if (connection.isClosed()) {
                System.err.println("✗ Database connection is closed when trying to delete player");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("✗ Error checking connection status: " + e.getMessage());
            return false;
        }
        
        String sql = "DELETE FROM playerss WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            System.out.println("=== DELETE OPERATION ===");
            System.out.println("Attempting to delete player with ID: " + id);
            System.out.println("Connection AutoCommit: " + connection.getAutoCommit());
            
            int affectedRows = pstmt.executeUpdate();
            System.out.println("Affected rows: " + affectedRows);
            
            // Explicitly commit if not in autocommit mode
            if (!connection.getAutoCommit()) {
                connection.commit();
                System.out.println("Changes committed to database");
            }
            
            if (affectedRows > 0) {
                System.out.println("✓ Player deleted from database (ID: " + id + ")");
                System.out.println("========================");
                return true;
            } else {
                System.err.println("✗ No player found with ID: " + id + " in database");
                System.out.println("========================");
            }
        } catch (SQLException e) {
            System.err.println("✗ Error deleting player: " + e.getMessage());
            e.printStackTrace();
            try {
                if (!connection.getAutoCommit()) {
                    connection.rollback();
                }
            } catch (SQLException rollbackEx) {
                System.err.println("✗ Error during rollback: " + rollbackEx.getMessage());
            }
        }
        return false;
    }

    /**
     * Get total number of players
     */
    public int count() {
        Connection connection = DatabaseConnection.getInstance().getConnection();
        String sql = "SELECT COUNT(*) FROM players";
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
        try {
            String clubView = rs.getString("club_view");
            if (!rs.wasNull()) {
                player.setClubView(clubView);
            }
        } catch (SQLException ignore) {
            // older DBs may not have column; ignore
        }
        return player;
    }
}

