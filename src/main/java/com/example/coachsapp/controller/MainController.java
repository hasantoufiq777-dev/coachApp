package com.example.coachsapp.controller;

import com.example.coachsapp.util.SceneSwitcher;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class MainController {

    @FXML
    public void goToClubs(ActionEvent event) {
        SceneSwitcher.switchTo(event, "club-view.fxml");
    }

    @FXML
    public void goToPlayers(ActionEvent event) {
        SceneSwitcher.switchTo(event, "player-list-view.fxml");
    }

    @FXML
    public void goToManagers(ActionEvent event) {
        SceneSwitcher.switchTo(event, "manager-view.fxml");
    }

    @FXML
    public void goToTransfer(ActionEvent event) {
        SceneSwitcher.switchTo(event, "transfer-view.fxml");
    }
}

