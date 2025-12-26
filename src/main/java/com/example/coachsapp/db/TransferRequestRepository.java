package com.example.coachsapp.db;

import com.example.coachsapp.model.TransferRequest;
import com.example.coachsapp.model.TransferRequest.TransferStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransferRequestRepository {
    
    public void createTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS transfer_requests (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                player_id INTEGER NOT NULL,
                source_club_id INTEGER NOT NULL,
                destination_club_id INTEGER,
                status TEXT NOT NULL,
                transfer_fee REAL DEFAULT 0.0,
                request_date TEXT NOT NULL,
                approved_by_source_date TEXT,
                completed_date TEXT,
                remarks TEXT,
                FOREIGN KEY (player_id) REFERENCES players(id),
                FOREIGN KEY (source_club_id) REFERENCES clubs(id),
                FOREIGN KEY (destination_club_id) REFERENCES clubs(id)
            )
        """;
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("✓ Transfer requests table created/verified");
            migrateAddTransferFee();
        } catch (SQLException e) {
            System.err.println("Error creating transfer_requests table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void migrateAddTransferFee() {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "transfer_requests", "transfer_fee");
            
            if (!columns.next()) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE transfer_requests ADD COLUMN transfer_fee REAL DEFAULT 0.0");
                    System.out.println("✓ Added transfer_fee column to transfer_requests table");
                }
            }
            
            // Also make destination_club_id nullable for general market requests
            ResultSet destClubColumn = metaData.getColumns(null, null, "transfer_requests", "destination_club_id");
            if (destClubColumn.next()) {
                int nullable = destClubColumn.getInt("NULLABLE");
                if (nullable == DatabaseMetaData.columnNoNulls) {
                    System.out.println("Note: destination_club_id nullability cannot be changed in SQLite. Recreate table if needed.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error migrating transfer_requests table: " + e.getMessage());
        }
    }

    public TransferRequest save(TransferRequest request) {
        if (request.getId() == null) {
            return insert(request);
        } else {
            return update(request);
        }
    }

    private TransferRequest insert(TransferRequest request) {
        String sql = "INSERT INTO transfer_requests (player_id, source_club_id, destination_club_id, status, transfer_fee, request_date, approved_by_source_date, completed_date, remarks) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, request.getPlayerId());
            pstmt.setInt(2, request.getSourceClubId());
            pstmt.setObject(3, request.getDestinationClubId(), Types.INTEGER);
            pstmt.setString(4, request.getStatus().name());
            pstmt.setDouble(5, request.getTransferFee() != null ? request.getTransferFee() : 0.0);
            pstmt.setString(6, request.getRequestDate().toString());
            pstmt.setString(7, request.getApprovedBySourceDate() != null ? request.getApprovedBySourceDate().toString() : null);
            pstmt.setString(8, request.getCompletedDate() != null ? request.getCompletedDate().toString() : null);
            pstmt.setString(9, request.getRemarks());
            
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                request.setId(rs.getInt(1));
            }
            return request;
        } catch (SQLException e) {
            System.err.println("Error inserting transfer request: " + e.getMessage());
        }
        return null;
    }

    private TransferRequest update(TransferRequest request) {
        String sql = "UPDATE transfer_requests SET player_id = ?, source_club_id = ?, destination_club_id = ?, status = ?, transfer_fee = ?, request_date = ?, approved_by_source_date = ?, completed_date = ?, remarks = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, request.getPlayerId());
            pstmt.setInt(2, request.getSourceClubId());
            pstmt.setObject(3, request.getDestinationClubId(), Types.INTEGER);
            pstmt.setString(4, request.getStatus().name());
            pstmt.setDouble(5, request.getTransferFee() != null ? request.getTransferFee() : 0.0);
            pstmt.setString(6, request.getRequestDate().toString());
            pstmt.setString(7, request.getApprovedBySourceDate() != null ? request.getApprovedBySourceDate().toString() : null);
            pstmt.setString(8, request.getCompletedDate() != null ? request.getCompletedDate().toString() : null);
            pstmt.setString(9, request.getRemarks());
            pstmt.setInt(10, request.getId());
            
            pstmt.executeUpdate();
            return request;
        } catch (SQLException e) {
            System.err.println("Error updating transfer request: " + e.getMessage());
        }
        return null;
    }

    public List<TransferRequest> findAll() {
        List<TransferRequest> requests = new ArrayList<>();
        String sql = """
            SELECT tr.*, 
                   p.name as player_name,
                   sc.name as source_club_name,
                   dc.name as destination_club_name
            FROM transfer_requests tr
            JOIN players p ON tr.player_id = p.id
            JOIN clubs sc ON tr.source_club_id = sc.id
            LEFT JOIN clubs dc ON tr.destination_club_id = dc.id
            ORDER BY tr.request_date DESC
        """;
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                requests.add(extractTransferRequestFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all transfer requests: " + e.getMessage());
        }
        return requests;
    }

    public List<TransferRequest> findInMarket() {
        List<TransferRequest> requests = new ArrayList<>();
        String sql = """
            SELECT tr.*, 
                   p.name as player_name,
                   sc.name as source_club_name,
                   dc.name as destination_club_name
            FROM transfer_requests tr
            JOIN players p ON tr.player_id = p.id
            JOIN clubs sc ON tr.source_club_id = sc.id
            LEFT JOIN clubs dc ON tr.destination_club_id = dc.id
            WHERE tr.status = 'IN_MARKET'
            ORDER BY tr.request_date DESC
        """;
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                requests.add(extractTransferRequestFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding market transfer requests: " + e.getMessage());
        }
        return requests;
    }

    public List<TransferRequest> findBySourceClubId(Integer clubId) {
        List<TransferRequest> requests = new ArrayList<>();
        String sql = """
            SELECT tr.*, 
                   p.name as player_name,
                   sc.name as source_club_name,
                   dc.name as destination_club_name
            FROM transfer_requests tr
            JOIN players p ON tr.player_id = p.id
            JOIN clubs sc ON tr.source_club_id = sc.id
            LEFT JOIN clubs dc ON tr.destination_club_id = dc.id
            WHERE tr.source_club_id = ?
            ORDER BY tr.request_date DESC
        """;
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, clubId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                requests.add(extractTransferRequestFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding transfer requests by source club: " + e.getMessage());
        }
        return requests;
    }

    public List<TransferRequest> findByDestinationClubId(Integer clubId) {
        List<TransferRequest> requests = new ArrayList<>();
        String sql = """
            SELECT tr.*, 
                   p.name as player_name,
                   sc.name as source_club_name,
                   dc.name as destination_club_name
            FROM transfer_requests tr
            JOIN players p ON tr.player_id = p.id
            JOIN clubs sc ON tr.source_club_id = sc.id
            LEFT JOIN clubs dc ON tr.destination_club_id = dc.id
            WHERE tr.destination_club_id = ?
            ORDER BY tr.request_date DESC
        """;
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, clubId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                requests.add(extractTransferRequestFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding transfer requests by destination club: " + e.getMessage());
        }
        return requests;
    }

    public List<TransferRequest> findByPlayerId(Integer playerId) {
        List<TransferRequest> requests = new ArrayList<>();
        String sql = """
            SELECT tr.*, 
                   p.name as player_name,
                   sc.name as source_club_name,
                   dc.name as destination_club_name
            FROM transfer_requests tr
            JOIN players p ON tr.player_id = p.id
            JOIN clubs sc ON tr.source_club_id = sc.id
            LEFT JOIN clubs dc ON tr.destination_club_id = dc.id
            WHERE tr.player_id = ?
            ORDER BY tr.request_date DESC
        """;
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, playerId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                requests.add(extractTransferRequestFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding transfer requests by player: " + e.getMessage());
        }
        return requests;
    }

    public TransferRequest findById(Integer id) {
        String sql = """
            SELECT tr.*, 
                   p.name as player_name,
                   sc.name as source_club_name,
                   dc.name as destination_club_name
            FROM transfer_requests tr
            JOIN players p ON tr.player_id = p.id
            JOIN clubs sc ON tr.source_club_id = sc.id
            LEFT JOIN clubs dc ON tr.destination_club_id = dc.id
            WHERE tr.id = ?
        """;
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return extractTransferRequestFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error finding transfer request by id: " + e.getMessage());
        }
        return null;
    }

    public void delete(Integer id) {
        String sql = "DELETE FROM transfer_requests WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting transfer request: " + e.getMessage());
        }
    }

    private TransferRequest extractTransferRequestFromResultSet(ResultSet rs) throws SQLException {
        TransferRequest request = new TransferRequest();
        request.setId(rs.getInt("id"));
        request.setPlayerId(rs.getInt("player_id"));
        request.setSourceClubId(rs.getInt("source_club_id"));
        
        Object destClubId = rs.getObject("destination_club_id");
        request.setDestinationClubId(destClubId != null ? (Integer) destClubId : null);
        
        request.setStatus(TransferStatus.valueOf(rs.getString("status")));
        request.setTransferFee(rs.getDouble("transfer_fee"));
        
        String requestDate = rs.getString("request_date");
        if (requestDate != null) {
            request.setRequestDate(LocalDateTime.parse(requestDate));
        }
        
        String approvedBySourceDate = rs.getString("approved_by_source_date");
        if (approvedBySourceDate != null) {
            request.setApprovedBySourceDate(LocalDateTime.parse(approvedBySourceDate));
        }
        
        String completedDate = rs.getString("completed_date");
        if (completedDate != null) {
            request.setCompletedDate(LocalDateTime.parse(completedDate));
        }
        
        request.setRemarks(rs.getString("remarks"));
        
        // Set display names
        request.setPlayerName(rs.getString("player_name"));
        request.setSourceClubName(rs.getString("source_club_name"));
        String destClubName = rs.getString("destination_club_name");
        request.setDestinationClubName(destClubName != null ? destClubName : "General Market");
        
        return request;
    }
}
