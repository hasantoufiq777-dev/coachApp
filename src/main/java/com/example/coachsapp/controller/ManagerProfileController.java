package com.example.coachsapp.controller;

import com.example.coachsapp.model.Manager;
import com.example.coachsapp.model.Player;
import com.example.coachsapp.model.Club;
import com.example.coachsapp.util.AppState;
import com.example.coachsapp.util.SceneSwitcher;
import com.example.coachsapp.db.ManagerRepository;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class ManagerProfileController {

    @FXML
    private Label managerNameLabel;

    @FXML
    private Label managerIdLabel;

    @FXML
    private Label managerAgeLabel;

    @FXML
    private Label clubNameLabel;

    @FXML
    private Label clubIdLabel;

    @FXML
    private TableView<Player> squadTable;

    @FXML
    private TableColumn<Player, String> squadNameColumn;

    @FXML
    private TableColumn<Player, Integer> squadJerseyColumn;

    @FXML
    private TableColumn<Player, String> squadPositionColumn;

    @FXML
    private TableColumn<Player, String> squadStatusColumn;

    private Manager currentManager;
    private ManagerRepository managerRepository = new ManagerRepository();

    @FXML
    public void initialize() {
        // Get the selected manager from AppState
        currentManager = AppState.getSelectedManager();
        
        if (currentManager != null) {
            setupTableColumns();
            loadManagerProfile();
        } else {
            showError("No manager selected");
        }
    }

    private void setupTableColumns() {
        squadNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        squadNameColumn.setCellFactory(col -> new TableCell<Player, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: black; -fx-font-weight: 600;");
                }
            }
        });
        squadJerseyColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getJersey()).asObject());
        squadPositionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPosition().toString()));
        squadStatusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().isInjured() ? "Injured" : "Available"));
    }

    private void loadManagerProfile() {
        managerNameLabel.setText(currentManager.getName());
        

        if (currentManager.getId() != null) {
            managerIdLabel.setText(String.valueOf(currentManager.getId()));
        } else {
            managerIdLabel.setText("N/A");
        }
        
        if (currentManager.getAge() != null) {
            managerAgeLabel.setText(String.valueOf(currentManager.getAge()));
        } else {
            managerAgeLabel.setText("N/A");
        }

        Club club = currentManager.getClub();
        if (club != null) {
            clubNameLabel.setText(club.getClubName());
            if (club.getId() != null) {
                clubIdLabel.setText(String.valueOf(club.getId()));
            } else {
                clubIdLabel.setText("N/A");
            }
            

            loadSquadPlayers(club);
        } else {
            clubNameLabel.setText("No Club");
            clubIdLabel.setText("N/A");
        }
    }

    private void loadSquadPlayers(Club club) {

        var clubPlayers = AppState.players.stream()
            .filter(p -> p.getClubId() != null && p.getClubId().equals(club.getId()))
            .toList();
        
        squadTable.setItems(FXCollections.observableArrayList(clubPlayers));
    }

    @FXML
    public void editManager(ActionEvent event) {
        // TODO: Implement edit functionality
        showInfo("Edit functionality will be implemented soon");
    }

    @FXML
    public void changeClub(ActionEvent event) {
        // TODO: Implement change club functionality
        showInfo("Change club functionality will be implemented soon");
    }

    @FXML
    public void goBack(ActionEvent event) {
        AppState.setSelectedManager(null); // Clear selection
        SceneSwitcher.switchTo(event, "manager-view.fxml");
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
