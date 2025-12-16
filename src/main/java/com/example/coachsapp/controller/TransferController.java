package com.example.coachsapp.controller;

import com.example.coachsapp.model.Manager;
import com.example.coachsapp.model.Player;
import com.example.coachsapp.service.TransferService;
import com.example.coachsapp.util.AppState;
import com.example.coachsapp.util.SceneSwitcher;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;

public class TransferController {

    @FXML
    private ComboBox<Manager> fromManagerCombo;

    @FXML
    private ComboBox<Manager> toManagerCombo;

    @FXML
    private ComboBox<Player> playerCombo;

    @FXML
    public void initialize() {
        fromManagerCombo.setItems(FXCollections.observableArrayList(AppState.managers));
        toManagerCombo.setItems(FXCollections.observableArrayList(AppState.managers));

        fromManagerCombo.setOnAction(e -> updatePlayerCombo());
    }

    private void updatePlayerCombo() {
        Manager selected = fromManagerCombo.getValue();
        if (selected != null) {
            playerCombo.setItems(FXCollections.observableArrayList(selected.getClub().getPlayers()));
        } else {
            playerCombo.setItems(FXCollections.observableArrayList());
        }
    }

    @FXML
    public void transferPlayer() {
        Manager fromManager = fromManagerCombo.getValue();
        Manager toManager = toManagerCombo.getValue();
        Player player = playerCombo.getValue();

        if (fromManager == null || toManager == null || player == null) {
            System.out.println("✗ Error: All fields must be selected!");
            showError("Please select both managers and a player");
            return;
        }

        if (fromManager.equals(toManager)) {
            System.out.println("✗ Error: Cannot transfer to the same club!");
            showError("Cannot transfer to the same club!");
            return;
        }

        boolean success = TransferService.transferPlayer(fromManager, toManager, player);
        if (success) {
            System.out.println("✓ Transfer successful: " + player.getName() +
                             " from " + fromManager.getClub().getClubName() +
                             " to " + toManager.getClub().getClubName());
            updatePlayerCombo();
            // Clear selections to avoid confusion
            playerCombo.setValue(null);
            toManagerCombo.setValue(null);
        } else {
            System.out.println("✗ Transfer failed!");
            showError("Transfer failed. Please check your selections.");
        }
    }

    private void showError(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
        alert.setTitle("Transfer Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void goBack(ActionEvent event) {
        SceneSwitcher.switchTo(event, "main-view.fxml");
    }
}

