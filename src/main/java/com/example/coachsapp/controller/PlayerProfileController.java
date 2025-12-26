package com.example.coachsapp.controller;

import com.example.coachsapp.model.Player;
import com.example.coachsapp.model.Role;
import com.example.coachsapp.model.User;
import com.example.coachsapp.util.AppState;
import com.example.coachsapp.util.SceneSwitcher;
import com.example.coachsapp.db.PlayerRepository;
import com.example.coachsapp.db.DatabaseService;
import com.example.coachsapp.dialog.EditPlayerDialog;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

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

    @FXML
    private Button editPlayerBtn;

    private Player currentPlayer;
    private DatabaseService dbService;

    @FXML
    public void initialize() {
        dbService = DatabaseService.getInstance();
        currentPlayer = AppState.getSelectedPlayer();
        
        User currentUser = AppState.currentUser;
        if (currentUser != null && currentUser.getRole() == Role.PLAYER) {
            editPlayerBtn.setVisible(false);
            editPlayerBtn.setManaged(false);
        }
        
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
        
        if (currentPlayer.isInjured()) {
            statusLabel.setText("Injured");
            statusLabel.setStyle("-fx-font-size: 14px; -fx-padding: 4 12; -fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 12px;");
        } else {
            statusLabel.setText("Available");
            statusLabel.setStyle("-fx-font-size: 14px; -fx-padding: 4 12; -fx-background-color: #10b981; -fx-text-fill: white; -fx-background-radius: 12px;");
        }
        
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
        
        if (currentPlayer.getId() != null) {
            playerIdLabel.setText(String.valueOf(currentPlayer.getId()));
        } else {
            playerIdLabel.setText("N/A");
        }
    }

    @FXML
    public void editPlayer(ActionEvent event) {
        if (currentPlayer == null) {
            showError("No player selected");
            return;
        }

        Stage stage = (Stage) playerNameLabel.getScene().getWindow();
        EditPlayerDialog dialog = new EditPlayerDialog(currentPlayer);
        Player updatedPlayer = dialog.showDialog(stage);

        if (updatedPlayer != null) {
            boolean updated = dbService.getPlayerRepository().update(updatedPlayer);
            
            if (updated) {

                AppState.players.stream()
                    .filter(p -> p.getId() != null && p.getId().equals(updatedPlayer.getId()))
                    .findFirst()
                    .ifPresent(p -> {
                        p.setName(updatedPlayer.getName());
                        p.setAge(updatedPlayer.getAge());
                        p.setJersey(updatedPlayer.getJersey());
                        p.setPosition(updatedPlayer.getPosition());
                        p.setClubId(updatedPlayer.getClubId());
                        p.setInjured(updatedPlayer.isInjured());
                    });
                
                currentPlayer = updatedPlayer;
                loadPlayerProfile();
                
                showInfo("Player details updated successfully!");
                System.out.println("✓ Player updated: " + updatedPlayer.getName());
            } else {
                showError("Failed to update player in database");
            }
        }
    }

    @FXML
    public void toggleInjuryStatus(ActionEvent event) {
        if (currentPlayer != null) {
            boolean newStatus = !currentPlayer.isInjured();
            currentPlayer.setInjured(newStatus);
            
            boolean updated = dbService.getPlayerRepository().update(currentPlayer);
            if (updated) {

                AppState.players.stream()
                    .filter(p -> p.getId() != null && p.getId().equals(currentPlayer.getId()))
                    .findFirst()
                    .ifPresent(p -> p.setInjured(newStatus));
                
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
        User currentUser = AppState.currentUser;
        
        if (currentUser != null && currentUser.getRole() == Role.PLAYER) {
            AppState.setSelectedPlayer(null);
            SceneSwitcher.switchTo(event, "main-view.fxml");
        } else {
            AppState.setSelectedPlayer(null);
            SceneSwitcher.switchTo(event, "player-view.fxml");
        }
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
