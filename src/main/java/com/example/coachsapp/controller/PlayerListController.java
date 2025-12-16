package com.example.coachsapp.controller;

import com.example.coachsapp.model.Player;
import com.example.coachsapp.model.Manager;
import com.example.coachsapp.util.SceneSwitcher;
import com.example.coachsapp.util.AppState;
import com.example.coachsapp.dialog.AddPlayerDialog;
import com.example.coachsapp.db.PlayerRepository;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableCell;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;

public class PlayerListController {

    @FXML
    private TableView<Player> playerTable;

    @FXML
    private TableColumn<Player, String> nameColumn;

    @FXML
    private TableColumn<Player, Integer> ageColumn;

    @FXML
    private TableColumn<Player, Integer> jerseyColumn;

    @FXML
    private TableColumn<Player, String> positionColumn;

    @FXML
    private TableColumn<Player, String> clubColumn;

    @FXML
    private TableColumn<Player, String> statusColumn;

    @FXML
    public void initialize() {
        // Use lambda-based factories to avoid reflection issues
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        ageColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getAge()));
        jerseyColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getJersey()));
        positionColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getPosition().toString()));

        clubColumn.setCellValueFactory(cellData -> {
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

        statusColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(
                cellData.getValue().isInjured() ? "Injured" : "Available"));

        // Style the Name column text with white bold text for better visibility
        nameColumn.setCellFactory(col -> new TableCell<Player, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    // white bold text for cells
                    setStyle("-fx-text-fill: #ffffff; -fx-font-weight: 900; -fx-font-size: 15px;");
                }
            }
        });

        // Load all players from all clubs
        loadAllPlayers();

        // Bind the table items to the global observable list
        playerTable.setItems(AppState.players);
    }

    private void loadAllPlayers() {
        // Collect all players from all clubs
        java.util.Set<Player> allPlayersFromClubs = new java.util.LinkedHashSet<>();
        for (Manager manager : AppState.managers) {
            allPlayersFromClubs.addAll(manager.getClub().getPlayers());
        }

        // Sync AppState.players with actual club players
        AppState.players.clear();
        AppState.players.addAll(allPlayersFromClubs);

        System.out.println("✓ Loaded " + AppState.players.size() + " players from all clubs");
    }

    @FXML
    public void addPlayer() {
        Stage stage = (Stage) playerTable.getScene().getWindow();
        AddPlayerDialog dialog = new AddPlayerDialog();
        Player newPlayer = dialog.showDialog(stage);
        
        if (newPlayer != null) {
            // Persist player and update AppState
            PlayerRepository repo = new PlayerRepository();
            Player saved = repo.save(newPlayer);
            if (saved != null) {
                AppState.players.add(saved);
                if (saved.getClubId() != null) {
                    AppState.clubs.stream()
                        .filter(c -> c.getId() != null && c.getId().equals(saved.getClubId()))
                        .findFirst()
                        .ifPresent(c -> c.addPlayer(saved));
                }
                playerTable.refresh();
                System.out.println("✓ Player added to table: " + saved.getName() + " - #" + saved.getJersey() + " (" + saved.getPosition() + ")");
            } else {
                System.err.println("✗ Failed to save player to database: " + newPlayer.getName());
            }
        }
    }

    @FXML
    public void deletePlayer() {
        Player selected = playerTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Find and remove from the club
            for (Manager manager : AppState.managers) {
                if (manager.getClub().removePlayer(selected)) {
                    break;
                }
            }
            loadAllPlayers();
            System.out.println("✓ Deleted: " + selected.getName());
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
                System.out.println("✓ Opening profile for: " + selected.getName());
            } catch (Exception e) {
                System.err.println("Error loading player profile: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("Please select a player to view profile");
        }
    }

    @FXML
    public void goBack(ActionEvent event) {
        SceneSwitcher.switchTo(event, "main-view.fxml");
    }
}
