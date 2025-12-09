package com.example.coachsapp.db;

import java.sql.*;

/**
 * Data Access Object (DAO) for Transfer History
 * Tracks all player transfers between clubs
 */
public class TransferHistoryRepository {

    private Connection connection;

    public TransferHistoryRepository() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Record a player transfer
     */
    public boolean recordTransfer(int playerId, Integer fromClubId, Integer toClubId) {
        String sql = "INSERT INTO transfer_history (player_id, from_club_id, to_club_id) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, playerId);
            pstmt.setObject(2, fromClubId, Types.INTEGER);
            pstmt.setObject(3, toClubId, Types.INTEGER);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("✓ Transfer recorded for player ID: " + playerId);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("✗ Error recording transfer: " + e.getMessage());
        }
        return false;
    }

    /**
     * Get transfer history for a player
     */
    public int getTransferCount(int playerId) {
        String sql = "SELECT COUNT(*) FROM transfer_history WHERE player_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, playerId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("✗ Error retrieving transfer count: " + e.getMessage());
        }
        return 0;
    }
}

