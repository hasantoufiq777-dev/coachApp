package com.example.coachsapp.controller;

import com.example.coachsapp.model.Position;
import com.example.coachsapp.model.Player;
import com.example.coachsapp.model.User;
import com.example.coachsapp.model.Role;
import com.example.coachsapp.model.Club;
import com.example.coachsapp.util.AppState;
import com.example.coachsapp.util.SceneSwitcher;
import com.example.coachsapp.dialog.AddPlayerDialog;
import com.example.coachsapp.db.DatabaseService;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.event.ActionEvent;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.stage.Stage;
import java.util.stream.Collectors;

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

    @FXML
    private ComboBox<String> clubFilterCombo;

    @FXML
    private ComboBox<String> positionFilterCombo;

    private DatabaseService dbService;
    private ObservableList<Player> basePlayerList; // Players visible to current user

    @FXML
    public void initialize() {
        dbService = DatabaseService.getInstance();
        setupTableColumns();
        setupFilters();
        loadBasePlayers();
        applyFilters();
        
        playerTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Player selected = playerTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    viewPlayerProfile(selected);
                }
            }
        });
    }

    private void setupFilters() {
        // Setup position filter
        ObservableList<String> positions = FXCollections.observableArrayList(
            "All Positions", "GOALKEEPER", "DEFENDER", "MIDFIELDER", "FORWARD"
        );
        positionFilterCombo.setItems(positions);
        positionFilterCombo.setValue("All Positions");
        positionFilterCombo.setOnAction(e -> applyFilters());

        // Setup club filter - will be populated in loadBasePlayers
        clubFilterCombo.setOnAction(e -> applyFilters());
    }

    private void loadBasePlayers() {
        User currentUser = AppState.currentUser;
        
        if (currentUser == null) {
            basePlayerList = FXCollections.observableArrayList();
            System.out.println("⚠ No user logged in");
            return;
        }

        if (currentUser.getRole() == Role.SYSTEM_ADMIN) {
            // Admin sees all players
            basePlayerList = FXCollections.observableArrayList(AppState.players);
            System.out.println("✓ Admin view: base list has " + basePlayerList.size() + " players");
            
            // Populate club filter with all clubs
            ObservableList<String> clubs = FXCollections.observableArrayList("All Clubs");
            clubs.addAll(AppState.clubs.stream()
                .map(Club::getClubName)
                .sorted()
                .collect(Collectors.toList()));
            clubFilterCombo.setItems(clubs);
            clubFilterCombo.setValue("All Clubs");
        } else if (currentUser.getRole() == Role.CLUB_MANAGER && currentUser.getClubId() != null) {
            // Manager sees only their club's players
            basePlayerList = FXCollections.observableArrayList(
                AppState.players.stream()
                    .filter(p -> p.getClubId() != null && p.getClubId().equals(currentUser.getClubId()))
                    .collect(Collectors.toList())
            );
            System.out.println("✓ Manager view: base list has " + basePlayerList.size() + " players from club ID " + currentUser.getClubId());
            
            // Manager's club filter is pre-filtered to their club only
            String managerClub = AppState.clubs.stream()
                .filter(c -> c.getId() != null && c.getId().equals(currentUser.getClubId()))
                .map(Club::getClubName)
                .findFirst()
                .orElse("My Club");
            ObservableList<String> clubs = FXCollections.observableArrayList("All Players", managerClub);
            clubFilterCombo.setItems(clubs);
            clubFilterCombo.setValue("All Players");
        } else {
            // Other roles see no players
            basePlayerList = FXCollections.observableArrayList();
            System.out.println("⚠ User role " + currentUser.getRole() + " has no player access");
        }
    }

    private void applyFilters() {
        if (basePlayerList == null) {
            return;
        }

        String selectedClub = clubFilterCombo.getValue();
        String selectedPosition = positionFilterCombo.getValue();
        
        ObservableList<Player> filteredPlayers = FXCollections.observableArrayList(basePlayerList);

        // Apply club filter
        if (selectedClub != null && !selectedClub.equals("All Clubs") && !selectedClub.equals("All Players")) {
            filteredPlayers = FXCollections.observableArrayList(
                filteredPlayers.stream()
                    .filter(p -> {
                        String playerClub = p.getClubView();
                        if (playerClub == null || playerClub.isEmpty()) {
                            playerClub = AppState.clubs.stream()
                                .filter(c -> c.getId() != null && c.getId().equals(p.getClubId()))
                                .map(Club::getClubName)
                                .findFirst()
                                .orElse("");
                        }
                        return playerClub.equals(selectedClub);
                    })
                    .collect(Collectors.toList())
            );
        }

        // Apply position filter
        if (selectedPosition != null && !selectedPosition.equals("All Positions")) {
            filteredPlayers = FXCollections.observableArrayList(
                filteredPlayers.stream()
                    .filter(p -> p.getPosition().toString().equals(selectedPosition))
                    .collect(Collectors.toList())
            );
        }

        playerTable.setItems(filteredPlayers);
        System.out.println("✓ Filters applied: showing " + filteredPlayers.size() + " players (Club: " + selectedClub + ", Position: " + selectedPosition + ")");
    }

    @FXML
    public void clearFilters() {
        clubFilterCombo.setValue(clubFilterCombo.getItems().get(0)); // First item is "All"
        positionFilterCombo.setValue("All Positions");
        applyFilters();
    }

    private void setupTableColumns() {
        playerNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        playerNameColumn.setCellFactory(col -> new TableCell<Player, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: black;");
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
         
            Player savedPlayer = dbService.getPlayerRepository().save(newPlayer);

            if (savedPlayer != null) {
             
                AppState.players.add(savedPlayer);

               
                if (savedPlayer.getClubId() != null) {
                    AppState.clubs.stream()
                        .filter(c -> c.getId() != null && c.getId().equals(savedPlayer.getClubId()))
                        .findFirst()
                        .ifPresent(club -> club.addPlayer(savedPlayer));
                }

                // No need to refresh - AppState.players is already bound
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
            System.out.println("=== Attempting to delete player: " + selected.getName() + " (ID: " + selected.getId() + ") ===");
    
            if (selected.getId() != null) {
                boolean deleted = dbService.getPlayerRepository().delete(selected.getId());
                if (deleted) {
                    System.out.println("✓ Player deleted from database: " + selected.getName());
                    
           
                    AppState.players.remove(selected);

                  
                    if (selected.getClubId() != null) {
                        AppState.clubs.stream()
                            .filter(c -> c.getId() != null && c.getId().equals(selected.getClubId()))
                            .findFirst()
                            .ifPresent(club -> club.removePlayer(selected));
                    }


                    System.out.println("✓ Player deleted successfully: " + selected.getName());
                    showInfo("Player deleted successfully!");
                } else {
                    System.out.println("✗ Failed to delete player from database");
                    showError("Failed to delete player from database");
                }
            } else {
            
                AppState.players.remove(selected);
                refreshPlayerTable();
                System.out.println("✓ Player removed (no database record)");
            }
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
        playerTable.refresh();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validation Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setContentText(message);
        alert.showAndWait();
    }
}

