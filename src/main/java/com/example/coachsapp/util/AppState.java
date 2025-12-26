package com.example.coachsapp.util;

import com.example.coachsapp.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

public class AppState {
    public static ObservableList<Manager> managers = FXCollections.observableArrayList();

    public static ObservableList<Player> players = FXCollections.observableArrayList();

    public static ObservableList<Club> clubs = FXCollections.observableArrayList();

    public static User currentUser = null;

    private static Player selectedPlayer;

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

    public static void refreshPlayers() {
        players.clear();
        for (Manager manager : managers) {
            if (manager.getClub() != null && manager.getClub().getPlayers() != null) {
                players.addAll(manager.getClub().getPlayers());
            }
        }
    }
}
