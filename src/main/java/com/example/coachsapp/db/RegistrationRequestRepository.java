package com.example.coachsapp.db;

import com.example.coachsapp.model.RegistrationRequest;
import com.example.coachsapp.model.RegistrationRequest.RequestStatus;
import com.example.coachsapp.model.Role;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RegistrationRequestRepository {
    
    public void createTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS registration_requests (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL,
                full_name TEXT NOT NULL,
                requested_role TEXT NOT NULL,
                club_id INTEGER NOT NULL,
                age INTEGER,
                position TEXT,
                status TEXT NOT NULL,
                request_date TEXT NOT NULL,
                approved_date TEXT,
                remarks TEXT,
                FOREIGN KEY (club_id) REFERENCES club(id)
            )
        """;
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            
            migrateAddAgeAndPosition(conn);
            
        } catch (SQLException e) {
            System.err.println("Error creating registration_requests table: " + e.getMessage());
        }
    }

    private void migrateAddAgeAndPosition(Connection conn) {
        try {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "registration_requests", "age");
            
            if (!columns.next()) {
                System.out.println("Adding 'age' column to registration_requests table...");
                Statement stmt = conn.createStatement();
                stmt.execute("ALTER TABLE registration_requests ADD COLUMN age INTEGER");
                stmt.close();
            }
            columns.close();
            
            columns = metaData.getColumns(null, null, "registration_requests", "position");
            if (!columns.next()) {
                System.out.println("Adding 'position' column to registration_requests table...");
                Statement stmt = conn.createStatement();
                stmt.execute("ALTER TABLE registration_requests ADD COLUMN position TEXT");
                stmt.close();
            }
            columns.close();
            
        } catch (SQLException e) {
            System.err.println("Error migrating registration_requests table: " + e.getMessage());
        }
    }

    public RegistrationRequest save(RegistrationRequest request) {
        if (request.getId() == null) {
            return insert(request);
        } else {
            return update(request);
        }
    }

    private RegistrationRequest insert(RegistrationRequest request) {
        String sql = "INSERT INTO registration_requests (username, password, full_name, requested_role, club_id, age, position, status, request_date, approved_date, remarks) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, request.getUsername());
            pstmt.setString(2, request.getPassword());
            pstmt.setString(3, request.getFullName());
            pstmt.setString(4, request.getRequestedRole().name());
            pstmt.setInt(5, request.getClubId());
            if (request.getAge() != null) {
                pstmt.setInt(6, request.getAge());
            } else {
                pstmt.setNull(6, Types.INTEGER);
            }
            pstmt.setString(7, request.getPosition());
            pstmt.setString(8, request.getStatus().name());
            pstmt.setString(9, request.getRequestDate().toString());
            pstmt.setString(10, request.getApprovedDate() != null ? request.getApprovedDate().toString() : null);
            pstmt.setString(11, request.getRemarks());
            
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                request.setId(rs.getInt(1));
            }
            return request;
        } catch (SQLException e) {
            System.err.println("Error inserting registration request: " + e.getMessage());
        }
        return null;
    }

    private RegistrationRequest update(RegistrationRequest request) {
        String sql = "UPDATE registration_requests SET username = ?, password = ?, full_name = ?, requested_role = ?, club_id = ?, age = ?, position = ?, status = ?, request_date = ?, approved_date = ?, remarks = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, request.getUsername());
            pstmt.setString(2, request.getPassword());
            pstmt.setString(3, request.getFullName());
            pstmt.setString(4, request.getRequestedRole().name());
            pstmt.setInt(5, request.getClubId());
            if (request.getAge() != null) {
                pstmt.setInt(6, request.getAge());
            } else {
                pstmt.setNull(6, Types.INTEGER);
            }
            pstmt.setString(7, request.getPosition());
            pstmt.setString(8, request.getStatus().name());
            pstmt.setString(9, request.getRequestDate().toString());
            pstmt.setString(10, request.getApprovedDate() != null ? request.getApprovedDate().toString() : null);
            pstmt.setString(11, request.getRemarks());
            pstmt.setInt(12, request.getId());
            
            pstmt.executeUpdate();
            return request;
        } catch (SQLException e) {
            System.err.println("Error updating registration request: " + e.getMessage());
        }
        return null;
    }

    public List<RegistrationRequest> findAll() {
        List<RegistrationRequest> requests = new ArrayList<>();
        String sql = """
            SELECT rr.*, c.name as club_name
            FROM registration_requests rr
            JOIN clubs c ON rr.club_id = c.id
            ORDER BY rr.request_date DESC
        """;
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                requests.add(extractFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all registration requests: " + e.getMessage());
        }
        return requests;
    }

    public List<RegistrationRequest> findByStatus(RequestStatus status) {
        List<RegistrationRequest> requests = new ArrayList<>();
        String sql = """
            SELECT rr.*, c.name as club_name
            FROM registration_requests rr
            JOIN clubs c ON rr.club_id = c.id
            WHERE rr.status = ?
            ORDER BY rr.request_date DESC
        """;
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status.name());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                requests.add(extractFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding registration requests by status: " + e.getMessage());
        }
        return requests;
    }

    public RegistrationRequest findById(Integer id) {
        String sql = """
            SELECT rr.*, c.name as club_name
            FROM registration_requests rr
            JOIN clubs c ON rr.club_id = c.id
            WHERE rr.id = ?
        """;
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return extractFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error finding registration request by id: " + e.getMessage());
        }
        return null;
    }

    public RegistrationRequest findByUsername(String username) {
        String sql = """
            SELECT rr.*, c.name as club_name
            FROM registration_requests rr
            JOIN clubs c ON rr.club_id = c.id
            WHERE rr.username = ?
        """;
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return extractFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error finding registration request by username: " + e.getMessage());
        }
        return null;
    }

    public void delete(Integer id) {
        String sql = "DELETE FROM registration_requests WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting registration request: " + e.getMessage());
        }
    }

    private RegistrationRequest extractFromResultSet(ResultSet rs) throws SQLException {
        RegistrationRequest request = new RegistrationRequest();
        request.setId(rs.getInt("id"));
        request.setUsername(rs.getString("username"));
        request.setPassword(rs.getString("password"));
        request.setFullName(rs.getString("full_name"));
        request.setRequestedRole(Role.valueOf(rs.getString("requested_role")));
        request.setClubId(rs.getInt("club_id"));
        request.setClubName(rs.getString("club_name"));
        
        int age = rs.getInt("age");
        if (!rs.wasNull()) {
            request.setAge(age);
        }
        request.setPosition(rs.getString("position"));
        
        request.setStatus(RequestStatus.valueOf(rs.getString("status")));
        
        String requestDate = rs.getString("request_date");
        if (requestDate != null) {
            request.setRequestDate(LocalDateTime.parse(requestDate));
        }
        
        String approvedDate = rs.getString("approved_date");
        if (approvedDate != null) {
            request.setApprovedDate(LocalDateTime.parse(approvedDate));
        }
        
        request.setRemarks(rs.getString("remarks"));
        
        return request;
    }
}
