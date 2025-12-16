package com.example.coachsapp.controller;

import com.example.coachsapp.model.Position;
import com.example.coachsapp.model.Player;
import com.example.coachsapp.util.AppState;
import com.example.coachsapp.util.SceneSwitcher;
import com.example.coachsapp.dialog.AddPlayerDialog;
import com.example.coachsapp.db.PlayerRepository;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Alert;
import javafx.collections.FXCollections;
import javafx.scene.control.TableCell;
import javafx.event.ActionEvent;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.stage.Stage;

public class PlayerController {

    @FXML
    private TableView<Player> playerTable;

    @FXML
    private TableColumn<Player, String> playerNameColumn;

    @FXML
    private TableColumn<Player, Integer> playerAgeColumn;

    @FXML
    private TableColumn<Player, Integer> playerJerseyColumn;

    @FXML
    private TableColumn<Player, String> playerPositionColumn;

    @FXML
    private TableColumn<Player, String> playerStatusColumn;

    @FXML
    private TableColumn<Player, String> playerClubColumn;

    private PlayerRepository playerRepository = new PlayerRepository();

    @FXML
    public void initialize() {
        setupTableColumns();
        refreshPlayerTable();
        
        // Add double-click handler to view player profile
        playerTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Player selected = playerTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    viewPlayerProfile(selected);
                }
            }
        });
    }

    private void setupTableColumns() {
        playerNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        // style name cells with a purple/blue tint for better visibility
        playerNameColumn.setCellFactory(col -> new TableCell<Player, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #7c3aed; -fx-font-weight: 600;");
                }
            }
        });
        playerAgeColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getAge()).asObject());
        playerJerseyColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getJersey()).asObject());
        playerPositionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPosition().toString()));
        playerStatusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().isInjured() ? "Injured" : "Available"));
        playerClubColumn.setCellValueFactory(cellData -> {
            // Prefer the snapshot club view saved on the player (club_view). Fallback to club lookup by id.
            String clubView = cellData.getValue().getClubView();
            if (clubView != null && !clubView.isEmpty()) {
                return new SimpleStringProperty(clubView);
            }

            Integer clubId = cellData.getValue().getClubId();
            if (clubId != null) {
                return new SimpleStringProperty(AppState.clubs.stream()
                    .filter(c -> c.getId() != null && c.getId().equals(clubId))
                    .map(c -> c.getClubName())
                    .findFirst()
                    .orElse("Unknown Club"));
            }
            return new SimpleStringProperty("No Club");
        });
    }

    @FXML
    public void addPlayer() {
        Stage stage = (Stage) playerTable.getScene().getWindow();
        AddPlayerDialog dialog = new AddPlayerDialog();
        Player newPlayer = dialog.showDialog(stage);

        if (newPlayer != null) {
            // Save to database
            Player savedPlayer = playerRepository.save(newPlayer);

            if (savedPlayer != null) {
                // Add to AppState
                AppState.players.add(savedPlayer);

                // Also add to the club's player list
                if (savedPlayer.getClubId() != null) {
                    AppState.clubs.stream()
                        .filter(c -> c.getId() != null && c.getId().equals(savedPlayer.getClubId()))
                        .findFirst()
                        .ifPresent(club -> club.addPlayer(savedPlayer));
                }

                refreshPlayerTable();
                System.out.println("✓ Player added successfully: " + newPlayer.getName());
            } else {
                showError("Failed to save player to database");
            }
        }
    }

    @FXML
    public void deletePlayer() {
        Player selected = playerTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            AppState.players.remove(selected);

            // Delete from database if player has an ID
            if (selected.getId() != null) {
                boolean deleted = playerRepository.delete(selected.getId());
                if (deleted) {
                    System.out.println("✓ Player deleted from database: " + selected.getName());
                } else {
                    System.out.println("✗ Failed to delete player from database");
                }
            }

            // Remove from club's player list
            if (selected.getClubId() != null) {
                AppState.clubs.stream()
                    .filter(c -> c.getId() != null && c.getId().equals(selected.getClubId()))
                    .findFirst()
                    .ifPresent(club -> club.removePlayer(selected));
            }

            refreshPlayerTable();
            System.out.println("✓ Player deleted: " + selected.getName());
        } else {
            showError("Please select a player to delete");
        }
    }

    @FXML
    public void viewProfile(ActionEvent event) {
        Player selected = playerTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            AppState.setSelectedPlayer(selected);
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/coachsapp/player-profile-view.fxml"));
                javafx.scene.Scene scene = new javafx.scene.Scene(loader.load(), 1000, 700);
                javafx.stage.Stage stage = (javafx.stage.Stage) playerTable.getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } catch (Exception e) {
                System.err.println("Error loading player profile: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showError("Please select a player to view profile");
        }
    }

    private void viewPlayerProfile(Player player) {
        AppState.setSelectedPlayer(player);
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/coachsapp/player-profile-view.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load(), 1000, 700);
            javafx.stage.Stage stage = (javafx.stage.Stage) playerTable.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            System.err.println("Error loading player profile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void goBack(ActionEvent event) {
        SceneSwitcher.switchTo(event, "main-view.fxml");
    }

    private void refreshPlayerTable() {
        playerTable.setItems(FXCollections.observableArrayList(AppState.players));
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validation Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}

