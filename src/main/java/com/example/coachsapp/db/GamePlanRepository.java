package com.example.coachsapp.db;

import com.example.coachsapp.model.GamePlan;
import com.example.coachsapp.model.Player;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for GamePlan database operations
 */
public class GamePlanRepository {

    private PlayerRepository playerRepository;

    public GamePlanRepository() {
        this.playerRepository = new PlayerRepository();
        createTable();
    }

    /**
     * Create the game_plans table if it doesn't exist
     */
    public void createTable() {
        Connection connection = DatabaseConnection.getInstance().getConnection();
        String sql = "CREATE TABLE IF NOT EXISTS game_plans (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                     "manager_id INTEGER NOT NULL, " +
                     "club_id INTEGER NOT NULL, " +
                     "name TEXT NOT NULL, " +
                     "goalkeeper_id INTEGER, " +
                     "defender1_id INTEGER, " +
                     "defender2_id INTEGER, " +
                     "midfielder1_id INTEGER, " +
                     "midfielder2_id INTEGER, " +
                     "forward_id INTEGER, " +
                     "created_at TEXT NOT NULL, " +
                     "updated_at TEXT NOT NULL, " +
                     "FOREIGN KEY (manager_id) REFERENCES managers(id) ON DELETE CASCADE, " +
                     "FOREIGN KEY (club_id) REFERENCES clubs(id) ON DELETE CASCADE, " +
                     "FOREIGN KEY (goalkeeper_id) REFERENCES players(id) ON DELETE SET NULL, " +
                     "FOREIGN KEY (defender1_id) REFERENCES players(id) ON DELETE SET NULL, " +
                     "FOREIGN KEY (defender2_id) REFERENCES players(id) ON DELETE SET NULL, " +
                     "FOREIGN KEY (midfielder1_id) REFERENCES players(id) ON DELETE SET NULL, " +
                     "FOREIGN KEY (midfielder2_id) REFERENCES players(id) ON DELETE SET NULL, " +
                     "FOREIGN KEY (forward_id) REFERENCES players(id) ON DELETE SET NULL" +
                     ")";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.executeUpdate();
            System.out.println("✓ game_plans table created/verified");
        } catch (SQLException e) {
            System.err.println("✗ Error creating game_plans table: " + e.getMessage());
        }
    }

    /**
     * Save a new game plan or update existing one
     */
    public GamePlan save(GamePlan gamePlan) {
        Connection connection = DatabaseConnection.getInstance().getConnection();
        
        if (gamePlan.getId() == null) {
            // Insert new game plan
            String sql = "INSERT INTO game_plans (manager_id, club_id, name, goalkeeper_id, " +
                        "defender1_id, defender2_id, midfielder1_id, midfielder2_id, forward_id, " +
                        "created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, gamePlan.getManagerId());
                pstmt.setInt(2, gamePlan.getClubId());
                pstmt.setString(3, gamePlan.getName());
                setPlayerIds(pstmt, gamePlan);
                pstmt.setString(10, LocalDateTime.now().toString());
                pstmt.setString(11, LocalDateTime.now().toString());

                pstmt.executeUpdate();
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    gamePlan.setId(generatedKeys.getInt(1));
                    System.out.println("✓ Game plan saved: " + gamePlan.getName());
                    return gamePlan;
                }
            } catch (SQLException e) {
                System.err.println("✗ Error saving game plan: " + e.getMessage());
            }
        } else {
            // Update existing game plan
            String sql = "UPDATE game_plans SET name = ?, goalkeeper_id = ?, defender1_id = ?, " +
                        "defender2_id = ?, midfielder1_id = ?, midfielder2_id = ?, forward_id = ?, " +
                        "updated_at = ? WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, gamePlan.getName());
                setPlayerIds(pstmt, gamePlan);
                pstmt.setString(8, LocalDateTime.now().toString());
                pstmt.setInt(9, gamePlan.getId());

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("✓ Game plan updated: " + gamePlan.getName());
                    return gamePlan;
                }
            } catch (SQLException e) {
                System.err.println("✗ Error updating game plan: " + e.getMessage());
            }
        }
        return null;
    }

    /**
     * Helper method to set player IDs in PreparedStatement
     */
    private void setPlayerIds(PreparedStatement pstmt, GamePlan gamePlan) throws SQLException {
        int startIndex = (gamePlan.getId() == null) ? 4 : 2;
        
        setNullableInt(pstmt, startIndex, gamePlan.getGoalkeeperId());
        setNullableInt(pstmt, startIndex + 1, gamePlan.getDefender1Id());
        setNullableInt(pstmt, startIndex + 2, gamePlan.getDefender2Id());
        setNullableInt(pstmt, startIndex + 3, gamePlan.getMidfielder1Id());
        setNullableInt(pstmt, startIndex + 4, gamePlan.getMidfielder2Id());
        setNullableInt(pstmt, startIndex + 5, gamePlan.getForwardId());
    }

    /**
     * Helper method to set nullable integer values
     */
    private void setNullableInt(PreparedStatement pstmt, int index, Integer value) throws SQLException {
        if (value == null) {
            pstmt.setNull(index, Types.INTEGER);
        } else {
            pstmt.setInt(index, value);
        }
    }

    /**
     * Find game plan by ID with all player details
     */
    public GamePlan findById(int id) {
        Connection connection = DatabaseConnection.getInstance().getConnection();
        String sql = "SELECT * FROM game_plans WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToGamePlan(rs);
            }
        } catch (SQLException e) {
            System.err.println("✗ Error finding game plan: " + e.getMessage());
        }
        return null;
    }

    /**
     * Find all game plans for a specific manager
     */
    public List<GamePlan> findByManagerId(int managerId) {
        Connection connection = DatabaseConnection.getInstance().getConnection();
        List<GamePlan> gamePlans = new ArrayList<>();
        String sql = "SELECT * FROM game_plans WHERE manager_id = ? ORDER BY updated_at DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, managerId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                gamePlans.add(mapResultSetToGamePlan(rs));
            }
        } catch (SQLException e) {
            System.err.println("✗ Error retrieving game plans: " + e.getMessage());
        }
        return gamePlans;
    }

    /**
     * Find all game plans for a specific club
     */
    public List<GamePlan> findByClubId(int clubId) {
        Connection connection = DatabaseConnection.getInstance().getConnection();
        List<GamePlan> gamePlans = new ArrayList<>();
        String sql = "SELECT * FROM game_plans WHERE club_id = ? ORDER BY updated_at DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, clubId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                gamePlans.add(mapResultSetToGamePlan(rs));
            }
        } catch (SQLException e) {
            System.err.println("✗ Error retrieving game plans: " + e.getMessage());
        }
        return gamePlans;
    }

    /**
     * Delete a game plan by ID
     */
    public boolean delete(int id) {
        Connection connection = DatabaseConnection.getInstance().getConnection();
        String sql = "DELETE FROM game_plans WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✓ Game plan deleted (ID: " + id + ")");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("✗ Error deleting game plan: " + e.getMessage());
        }
        return false;
    }

    /**
     * Map ResultSet to GamePlan object
     */
    private GamePlan mapResultSetToGamePlan(ResultSet rs) throws SQLException {
        GamePlan gamePlan = new GamePlan();
        gamePlan.setId(rs.getInt("id"));
        gamePlan.setManagerId(rs.getInt("manager_id"));
        gamePlan.setClubId(rs.getInt("club_id"));
        gamePlan.setName(rs.getString("name"));

        // Load player IDs
        Integer gkId = getIntOrNull(rs, "goalkeeper_id");
        Integer def1Id = getIntOrNull(rs, "defender1_id");
        Integer def2Id = getIntOrNull(rs, "defender2_id");
        Integer mid1Id = getIntOrNull(rs, "midfielder1_id");
        Integer mid2Id = getIntOrNull(rs, "midfielder2_id");
        Integer fwdId = getIntOrNull(rs, "forward_id");

        gamePlan.setGoalkeeperId(gkId);
        gamePlan.setDefender1Id(def1Id);
        gamePlan.setDefender2Id(def2Id);
        gamePlan.setMidfielder1Id(mid1Id);
        gamePlan.setMidfielder2Id(mid2Id);
        gamePlan.setForwardId(fwdId);

        // Load actual player objects
        if (gkId != null) gamePlan.setGoalkeeper(playerRepository.findById(gkId));
        if (def1Id != null) gamePlan.setDefender1(playerRepository.findById(def1Id));
        if (def2Id != null) gamePlan.setDefender2(playerRepository.findById(def2Id));
        if (mid1Id != null) gamePlan.setMidfielder1(playerRepository.findById(mid1Id));
        if (mid2Id != null) gamePlan.setMidfielder2(playerRepository.findById(mid2Id));
        if (fwdId != null) gamePlan.setForward(playerRepository.findById(fwdId));

        // Parse timestamps
        String createdAt = rs.getString("created_at");
        String updatedAt = rs.getString("updated_at");
        if (createdAt != null) gamePlan.setCreatedAt(LocalDateTime.parse(createdAt));
        if (updatedAt != null) gamePlan.setUpdatedAt(LocalDateTime.parse(updatedAt));

        return gamePlan;
    }

    /**
     * Helper method to get Integer or null from ResultSet
     */
    private Integer getIntOrNull(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : value;
    }
}
