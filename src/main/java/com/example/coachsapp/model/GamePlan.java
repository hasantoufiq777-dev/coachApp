package com.example.coachsapp.model;

import java.time.LocalDateTime;

/**
 * Represents a game plan/lineup consisting of 6 players
 * Formation: 1 GK, 2 Defenders, 2 Midfielders, 1 Forward
 */
public class GamePlan {
    private Integer id;
    private Integer managerId;
    private Integer clubId;
    private String name; // Game plan name/title
    
    // Player positions
    private Integer goalkeeperId;
    private Integer defender1Id;
    private Integer defender2Id;
    private Integer midfielder1Id;
    private Integer midfielder2Id;
    private Integer forwardId;
    
    // For display purposes
    private Player goalkeeper;
    private Player defender1;
    private Player defender2;
    private Player midfielder1;
    private Player midfielder2;
    private Player forward;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public GamePlan() {
    }

    public GamePlan(Integer managerId, Integer clubId, String name) {
        this.managerId = managerId;
        this.clubId = clubId;
        this.name = name;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getManagerId() {
        return managerId;
    }

    public void setManagerId(Integer managerId) {
        this.managerId = managerId;
    }

    public Integer getClubId() {
        return clubId;
    }

    public void setClubId(Integer clubId) {
        this.clubId = clubId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getGoalkeeperId() {
        return goalkeeperId;
    }

    public void setGoalkeeperId(Integer goalkeeperId) {
        this.goalkeeperId = goalkeeperId;
    }

    public Integer getDefender1Id() {
        return defender1Id;
    }

    public void setDefender1Id(Integer defender1Id) {
        this.defender1Id = defender1Id;
    }

    public Integer getDefender2Id() {
        return defender2Id;
    }

    public void setDefender2Id(Integer defender2Id) {
        this.defender2Id = defender2Id;
    }

    public Integer getMidfielder1Id() {
        return midfielder1Id;
    }

    public void setMidfielder1Id(Integer midfielder1Id) {
        this.midfielder1Id = midfielder1Id;
    }

    public Integer getMidfielder2Id() {
        return midfielder2Id;
    }

    public void setMidfielder2Id(Integer midfielder2Id) {
        this.midfielder2Id = midfielder2Id;
    }

    public Integer getForwardId() {
        return forwardId;
    }

    public void setForwardId(Integer forwardId) {
        this.forwardId = forwardId;
    }

    public Player getGoalkeeper() {
        return goalkeeper;
    }

    public void setGoalkeeper(Player goalkeeper) {
        this.goalkeeper = goalkeeper;
        if (goalkeeper != null) {
            this.goalkeeperId = goalkeeper.getId();
        }
    }

    public Player getDefender1() {
        return defender1;
    }

    public void setDefender1(Player defender1) {
        this.defender1 = defender1;
        if (defender1 != null) {
            this.defender1Id = defender1.getId();
        }
    }

    public Player getDefender2() {
        return defender2;
    }

    public void setDefender2(Player defender2) {
        this.defender2 = defender2;
        if (defender2 != null) {
            this.defender2Id = defender2.getId();
        }
    }

    public Player getMidfielder1() {
        return midfielder1;
    }

    public void setMidfielder1(Player midfielder1) {
        this.midfielder1 = midfielder1;
        if (midfielder1 != null) {
            this.midfielder1Id = midfielder1.getId();
        }
    }

    public Player getMidfielder2() {
        return midfielder2;
    }

    public void setMidfielder2(Player midfielder2) {
        this.midfielder2 = midfielder2;
        if (midfielder2 != null) {
            this.midfielder2Id = midfielder2.getId();
        }
    }

    public Player getForward() {
        return forward;
    }

    public void setForward(Player forward) {
        this.forward = forward;
        if (forward != null) {
            this.forwardId = forward.getId();
        }
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Check if the gameplan is complete (all positions filled)
     */
    public boolean isComplete() {
        return goalkeeperId != null && defender1Id != null && defender2Id != null &&
               midfielder1Id != null && midfielder2Id != null && forwardId != null;
    }

    @Override
    public String toString() {
        return name + (isComplete() ? " ✓" : " (Incomplete)");
    }
}
