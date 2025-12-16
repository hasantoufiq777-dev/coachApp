package com.example.coachsapp.util;

import com.example.coachsapp.model.Club;
import com.example.coachsapp.model.Manager;
import com.example.coachsapp.model.Player;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

public class AppState {
    public static List<Manager> managers = new ArrayList<>();

    // Global observable list of players so UI tables can bind to it
    public static ObservableList<Player> players = FXCollections.observableArrayList();

    // Global observable list of clubs for club management
    public static ObservableList<Club> clubs = FXCollections.observableArrayList();

    // Currently selected player for profile view
    private static Player selectedPlayer;

    // Currently selected manager for profile view
    private static Manager selectedManager;

    public static Player getSelectedPlayer() {
        return selectedPlayer;
    }

    public static void setSelectedPlayer(Player player) {
        selectedPlayer = player;
    }

    public static Manager getSelectedManager() {
        return selectedManager;
    }

    public static void setSelectedManager(Manager manager) {
        selectedManager = manager;
    }
}
