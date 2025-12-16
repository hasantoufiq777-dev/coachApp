package com.example.coachsapp.controller;

import com.example.coachsapp.model.Player;
import com.example.coachsapp.util.AppState;
import com.example.coachsapp.util.SceneSwitcher;
import com.example.coachsapp.db.PlayerRepository;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.event.ActionEvent;

public class PlayerProfileController {

    @FXML
    private Label playerNameLabel;

    @FXML
    private Label jerseyLabel;

    @FXML
    private Label ageLabel;

    @FXML
    private Label positionLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Label clubLabel;

    @FXML
    private Label playerIdLabel;

    private Player currentPlayer;
    private PlayerRepository playerRepository = new PlayerRepository();

    @FXML
    public void initialize() {
        // Get the selected player from AppState
        currentPlayer = AppState.getSelectedPlayer();
        
        if (currentPlayer != null) {
            loadPlayerProfile();
        } else {
            showError("No player selected");
        }
    }

    private void loadPlayerProfile() {
        playerNameLabel.setText(currentPlayer.getName());
        jerseyLabel.setText(String.valueOf(currentPlayer.getJersey()));
        ageLabel.setText(String.valueOf(currentPlayer.getAge()));
        positionLabel.setText(currentPlayer.getPosition().toString());
        
        // Status with color coding
        if (currentPlayer.isInjured()) {
            statusLabel.setText("Injured");
            statusLabel.setStyle("-fx-font-size: 14px; -fx-padding: 4 12; -fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 12px;");
        } else {
            statusLabel.setText("Available");
            statusLabel.setStyle("-fx-font-size: 14px; -fx-padding: 4 12; -fx-background-color: #10b981; -fx-text-fill: white; -fx-background-radius: 12px;");
        }
        
        // Club information
        String clubView = currentPlayer.getClubView();
        if (clubView != null && !clubView.isEmpty()) {
            clubLabel.setText(clubView);
        } else if (currentPlayer.getClubId() != null) {
            clubLabel.setText(AppState.clubs.stream()
                .filter(c -> c.getId() != null && c.getId().equals(currentPlayer.getClubId()))
                .map(c -> c.getClubName())
                .findFirst()
                .orElse("Unknown Club"));
        } else {
            clubLabel.setText("No Club");
        }
        
        // Player ID
        if (currentPlayer.getId() != null) {
            playerIdLabel.setText(String.valueOf(currentPlayer.getId()));
        } else {
            playerIdLabel.setText("N/A");
        }
    }

    @FXML
    public void editPlayer(ActionEvent event) {
        // TODO: Implement edit functionality
        showInfo("Edit functionality will be implemented soon");
    }

    @FXML
    public void toggleInjuryStatus(ActionEvent event) {
        if (currentPlayer != null) {
            boolean newStatus = !currentPlayer.isInjured();
            currentPlayer.setInjured(newStatus);
            
            // Update in database
            boolean updated = playerRepository.update(currentPlayer);
            if (updated) {
                // Update in AppState
                AppState.players.stream()
                    .filter(p -> p.getId() != null && p.getId().equals(currentPlayer.getId()))
                    .findFirst()
                    .ifPresent(p -> p.setInjured(newStatus));
                
                // Refresh the display
                loadPlayerProfile();
                
                String statusMsg = newStatus ? "injured" : "available";
                System.out.println("✓ Player status updated to: " + statusMsg);
                showInfo("Player status updated to: " + statusMsg);
            } else {
                showError("Failed to update player status in database");
            }
        }
    }

    @FXML
    public void goBack(ActionEvent event) {
        AppState.setSelectedPlayer(null); // Clear selection
        SceneSwitcher.switchTo(event, "player-view.fxml");
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
