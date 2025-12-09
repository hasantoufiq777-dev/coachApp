package com.example.coachsapp.util;

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
}
