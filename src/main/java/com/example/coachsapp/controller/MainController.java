package com.example.coachsapp.controller;

import com.example.coachsapp.model.Role;
import com.example.coachsapp.model.User;
import com.example.coachsapp.util.AppState;
import com.example.coachsapp.util.SceneSwitcher;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class MainController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label roleLabel;

    @FXML
    private VBox buttonContainer;

    @FXML
    private Button managePlayersBtn;

    @FXML
    private Button manageManagersBtn;

    @FXML
    private Button manageClubsBtn;

    @FXML
    private Button transferRequestBtn;

    @FXML
    private Button transferMarketBtn;

    @FXML
    private Button approveRegistrationsBtn;

    @FXML
    private Button myProfileBtn;

    @FXML
    private Button logoutBtn;

    @FXML
    public void initialize() {
        User currentUser = AppState.currentUser;
        if (currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getUsername());
            roleLabel.setText("Role: " + currentUser.getRole().getDisplayName());
            
            setupUIForRole(currentUser.getRole());
        }
    }

    private void setupUIForRole(Role role) {
   
        approveRegistrationsBtn.setVisible(false);
        approveRegistrationsBtn.setManaged(false);
        managePlayersBtn.setVisible(false);
        managePlayersBtn.setManaged(false);
        manageManagersBtn.setVisible(false);
        manageManagersBtn.setManaged(false);
        manageClubsBtn.setVisible(false);
        manageClubsBtn.setManaged(false);
        transferRequestBtn.setVisible(false);
        transferRequestBtn.setManaged(false);
        transferMarketBtn.setVisible(false);
        transferMarketBtn.setManaged(false);
        myProfileBtn.setVisible(false);
        myProfileBtn.setManaged(false);

        switch (role) {
            case SYSTEM_ADMIN:
               
                approveRegistrationsBtn.setVisible(true);
                approveRegistrationsBtn.setManaged(true);
                managePlayersBtn.setVisible(true);
                managePlayersBtn.setManaged(true);
                manageManagersBtn.setVisible(true);
                manageManagersBtn.setManaged(true);
                manageClubsBtn.setVisible(true);
                manageClubsBtn.setManaged(true);
                transferRequestBtn.setVisible(true);
                transferRequestBtn.setManaged(true);
                transferMarketBtn.setVisible(true);
                transferMarketBtn.setManaged(true);
                break;

            case CLUB_OWNER:

                managePlayersBtn.setVisible(true);
                managePlayersBtn.setManaged(true);
                manageManagersBtn.setVisible(true);
                manageManagersBtn.setManaged(true);
                transferRequestBtn.setVisible(true);
                transferRequestBtn.setManaged(true);
                break;

            case CLUB_MANAGER:

                managePlayersBtn.setVisible(true);
                managePlayersBtn.setManaged(true);
                transferRequestBtn.setVisible(true);
                transferRequestBtn.setManaged(true);
                transferRequestBtn.setText("Transfer Approvals");
                transferMarketBtn.setVisible(true);
                transferMarketBtn.setManaged(true);
                break;

            case PLAYER:

                myProfileBtn.setVisible(true);
                myProfileBtn.setManaged(true);
                transferRequestBtn.setVisible(true);
                transferRequestBtn.setManaged(true);
                transferRequestBtn.setText("Request Transfer");
                transferMarketBtn.setVisible(true);
                transferMarketBtn.setManaged(true);
                transferMarketBtn.setText("View Transfer Market");
                break;
        }
    }

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
    public void goToTransferRequests(ActionEvent event) {
        SceneSwitcher.switchTo(event, "transfer-request-view.fxml");
    }

    @FXML
    public void goToTransferMarket(ActionEvent event) {
        SceneSwitcher.switchTo(event, "transfer-market-view.fxml");
    }

    @FXML
    public void goToApproveRegistrations(ActionEvent event) {
        SceneSwitcher.switchTo(event, "registration-approval-view.fxml");
    }

    @FXML
    public void goToMyProfile(ActionEvent event) {
        User currentUser = AppState.currentUser;
        if (currentUser != null && currentUser.getRole() == Role.PLAYER && currentUser.getPlayerId() != null) {
            com.example.coachsapp.db.DatabaseService dbService = com.example.coachsapp.db.DatabaseService.getInstance();
            com.example.coachsapp.model.Player player = dbService.getPlayerRepository().findById(currentUser.getPlayerId());
            
            if (player != null) {
                AppState.setSelectedPlayer(player);
                SceneSwitcher.switchTo(event, "player-profile-view.fxml");
            } else {
                System.err.println("Player profile not found for ID: " + currentUser.getPlayerId());
            }
        }
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        AppState.currentUser = null;
        SceneSwitcher.switchTo(event, "login-view.fxml");
    }
}

