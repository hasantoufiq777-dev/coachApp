package com.example.coachsapp.model;

public class User {
    private Integer id;
    private String username;
    private String password; // In production, this should be hashed
    private Role role;
    private Integer clubId; // null for SYSTEM_ADMIN
    private Integer playerId; // Only for PLAYER role
    private Integer managerId; // Only for CLUB_MANAGER role

    public User() {
    }

    public User(Integer id, String username, String password, Role role, Integer clubId, Integer playerId, Integer managerId) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.clubId = clubId;
        this.playerId = playerId;
        this.managerId = managerId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Integer getClubId() {
        return clubId;
    }

    public void setClubId(Integer clubId) {
        this.clubId = clubId;
    }

    public Integer getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Integer playerId) {
        this.playerId = playerId;
    }

    public Integer getManagerId() {
        return managerId;
    }

    public void setManagerId(Integer managerId) {
        this.managerId = managerId;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", role=" + role +
                ", clubId=" + clubId +
                ", playerId=" + playerId +
                ", managerId=" + managerId +
                '}';
    }
}
