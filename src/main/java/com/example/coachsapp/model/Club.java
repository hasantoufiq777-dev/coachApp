package com.example.coachsapp.model;

import java.util.ArrayList;
import java.util.List;

public class Club {
    private Integer id;
    private String clubName;
    private List<Player> players;

    public Club(String clubName) {
        this.clubName = clubName;
        this.players = new ArrayList<>();
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getClubName() {
        return clubName;
    }

    public void setClubName(String clubName) {
        this.clubName = clubName;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void addPlayer(Player player) {
        if (player != null) {
            players.add(player);
        }
    }

    public boolean removePlayer(Player player) {
        return players.remove(player);
    }

    @Override
    public String toString() {
        return "Club{" +
                "id=" + id +
                ", clubName='" + clubName + '\'' +
                ", players=" + players.size() +
                '}';
    }
}

