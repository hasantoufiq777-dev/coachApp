package com.example.coachsapp.model;

public class Manager {
    private Integer id;
    private String name;
    private Club club;

    public Manager(String managerName, String clubName) {
        this.name = managerName;
        this.club = new Club(clubName);
    }

    public Manager(String name, Club club) {
        this.name = name;
        this.club = club;
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Club getClub() {
        return club;
    }

    public void setClub(Club club) {
        this.club = club;
    }

    @Override
    public String toString() {
        return name + " (" + club.getClubName() + ")";
    }
}

