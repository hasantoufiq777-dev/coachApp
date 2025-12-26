package com.example.coachsapp.controller;

import com.example.coachsapp.model.Player;
import com.example.coachsapp.model.Manager;
import com.example.coachsapp.model.User;
import com.example.coachsapp.model.Role;
import com.example.coachsapp.model.Club;
import com.example.coachsapp.util.SceneSwitcher;
import com.example.coachsapp.util.AppState;
import com.example.coachsapp.dialog.AddPlayerDialog;
import com.example.coachsapp.dialog.EditPlayerDialog;
import com.example.coachsapp.db.PlayerRepository;
import com.example.coachsapp.db.DatabaseService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.stream.Collectors;

public class PlayerListController {

    @FXML
    private FlowPane playerCardsPane;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private ComboBox<String> clubFilterCombo;

    @FXML
    private ComboBox<String> positionFilterCombo;

    private DatabaseService dbService;
    private java.util.List<Player> basePlayerList; // Players visible to current user

    @FXML
    public void initialize() {
        dbService = DatabaseService.getInstance();
        System.out.println("✓ PlayerListController initialized with " + AppState.players.size() + " players");
        setupFilters();
        loadBasePlayers();
        applyFilters();
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
            basePlayerList = new java.util.ArrayList<>();
            System.out.println("⚠ No user logged in");
            return;
        }

        if (currentUser.getRole() == Role.SYSTEM_ADMIN) {
            // Admin sees all players
            basePlayerList = new java.util.ArrayList<>(AppState.players);
            System.out.println("✓ Admin card view: base list has " + basePlayerList.size() + " players");
            
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
            basePlayerList = AppState.players.stream()
                .filter(p -> p.getClubId() != null && p.getClubId().equals(currentUser.getClubId()))
                .collect(Collectors.toList());
            System.out.println("✓ Manager card view: base list has " + basePlayerList.size() + " players from club ID " + currentUser.getClubId());
            
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
            basePlayerList = new java.util.ArrayList<>();
            System.out.println("⚠ User role " + currentUser.getRole() + " has no player access");
        }
    }

    private void applyFilters() {
        if (basePlayerList == null) {
            return;
        }

        String selectedClub = clubFilterCombo.getValue();
        String selectedPosition = positionFilterCombo.getValue();
        
        java.util.List<Player> filteredPlayers = new java.util.ArrayList<>(basePlayerList);

        // Apply club filter
        if (selectedClub != null && !selectedClub.equals("All Clubs") && !selectedClub.equals("All Players")) {
            filteredPlayers = filteredPlayers.stream()
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
                .collect(Collectors.toList());
        }

        // Apply position filter
        if (selectedPosition != null && !selectedPosition.equals("All Positions")) {
            filteredPlayers = filteredPlayers.stream()
                .filter(p -> p.getPosition().toString().equals(selectedPosition))
                .collect(Collectors.toList());
        }

        // Recreate cards with filtered players
        createPlayerCards(filteredPlayers);
        System.out.println("✓ Card filters applied: showing " + filteredPlayers.size() + " player cards (Club: " + selectedClub + ", Position: " + selectedPosition + ")");
    }

    @FXML
    public void clearFilters() {
        clubFilterCombo.setValue(clubFilterCombo.getItems().get(0)); // First item is "All"
        positionFilterCombo.setValue("All Positions");
        applyFilters();
    }

    private void createPlayerCards(java.util.List<Player> playersToShow) {
        playerCardsPane.getChildren().clear();
        
        for (Player player : playersToShow) {
            VBox card = createPlayerCard(player);
            playerCardsPane.getChildren().add(card);
        }
    }

    private VBox createPlayerCard(Player player) {
        VBox card = new VBox(8);
        card.setPrefSize(220, 320);
        card.setMaxSize(220, 320);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(15));
        
        String statusColor = player.isInjured() ? "#ff4444" : "#4CAF50";
        String cardStyle = "-fx-background-color: linear-gradient(to bottom, #1a1a1a 0%, #2d2d2d 100%);" +
                          "-fx-border-color: " + statusColor + ";" +
                          "-fx-border-width: 3;" +
                          "-fx-border-radius: 10;" +
                          "-fx-background-radius: 10;" +
                          "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 10, 0, 0, 4);";
        card.setStyle(cardStyle);
        
        // Player icon
        Label playerIcon = new Label("⚽");
        playerIcon.setStyle("-fx-font-size: 60px; -fx-text-fill: white;");
        
        Label nameLabel = new Label(player.getName().toUpperCase());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(190);
        nameLabel.setAlignment(Pos.CENTER);
        
        String clubName = "No Club";
        if (player.getClubView() != null && !player.getClubView().isEmpty()) {
            clubName = player.getClubView();
        } else if (player.getClubId() != null) {
            clubName = AppState.clubs.stream()
                .filter(c -> c.getId() != null && c.getId().equals(player.getClubId()))
                .map(c -> c.getClubName())
                .findFirst()
                .orElse("Unknown Club");
        }
        
        VBox statsBox = new VBox(5);
        statsBox.setAlignment(Pos.CENTER_LEFT);
        statsBox.setPadding(new Insets(10, 0, 0, 0));
        statsBox.setStyle("-fx-background-color: rgba(0,0,0,0.3); -fx-padding: 10; -fx-background-radius: 5;");
        
        Label jerseyLabel = new Label("JERSEY: #" + player.getJersey());
        jerseyLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + statusColor + "; -fx-font-weight: bold;");
        
        Label posLabel = new Label("POS: " + player.getPosition().toString());
        posLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ffdd57; -fx-font-weight: bold;");
        
        Label ageLabel = new Label("AGE: " + player.getAge());
        ageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #aaa;");
        
        Label clubLabel = new Label("CLUB: " + clubName);
        clubLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #aaa;");
        clubLabel.setWrapText(true);
        clubLabel.setMaxWidth(180);
        
        Label statusLabel = new Label(player.isInjured() ? "🚑 INJURED" : "✓ AVAILABLE");
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + statusColor + "; -fx-font-weight: bold;");
        
        statsBox.getChildren().addAll(jerseyLabel, posLabel, ageLabel, clubLabel, statusLabel);
        
        HBox buttonBox = new HBox(8);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(8, 0, 0, 0));
        
        Button viewBtn = new Button("View");
        viewBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 15;");
        viewBtn.setOnAction(e -> viewPlayerProfile(player));
        
        Button editBtn = new Button("Edit");
        editBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 15;");
        editBtn.setOnAction(e -> editPlayer(player));
        
        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 15;");
        deleteBtn.setOnAction(e -> deletePlayer(player));
        
        buttonBox.getChildren().addAll(viewBtn, editBtn, deleteBtn);
        
        card.getChildren().addAll(playerIcon, nameLabel, statsBox, buttonBox);
        
        card.setOnMouseEntered(e -> {
            card.setStyle(cardStyle + "-fx-cursor: hand; -fx-scale-x: 1.05; -fx-scale-y: 1.05;");
        });
        card.setOnMouseExited(e -> {
            card.setStyle(cardStyle);
        });
        
        return card;
    }

    private void viewPlayerProfile(Player player) {
        AppState.setSelectedPlayer(player);
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/example/coachsapp/player-profile-view.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load(), 1000, 700);
            javafx.stage.Stage stage = (javafx.stage.Stage) playerCardsPane.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
            System.out.println("✓ Opening profile for: " + player.getName());
        } catch (Exception e) {
            System.err.println("Error loading player profile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void editPlayer(Player player) {
        Stage stage = (Stage) playerCardsPane.getScene().getWindow();
        EditPlayerDialog dialog = new EditPlayerDialog(player);
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
                        p.setInjured(updatedPlayer.isInjured());
                        p.setClubId(updatedPlayer.getClubId());
                        p.setClubView(updatedPlayer.getClubView());
                    });
                applyFilters();
                System.out.println("✓ Player updated: " + updatedPlayer.getName());
            }
        }
    }

    private void deletePlayer(Player player) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Player");
        confirm.setContentText("Are you sure you want to delete " + player.getName() + "?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (player.getId() != null) {
                    boolean deleted = dbService.getPlayerRepository().delete(player.getId());
                    if (deleted) {
                        AppState.players.remove(player);
                        if (player.getClubId() != null) {
                            AppState.clubs.stream()
                                .filter(c -> c.getId() != null && c.getId().equals(player.getClubId()))
                                .findFirst()
                                .ifPresent(club -> club.removePlayer(player));
                        }
                        applyFilters();
                        System.out.println("✓ Player deleted: " + player.getName());
                    }
                }
            }
        });
    }

    @FXML
    public void addPlayer() {
        Stage stage = (Stage) playerCardsPane.getScene().getWindow();
        AddPlayerDialog dialog = new AddPlayerDialog();
        Player newPlayer = dialog.showDialog(stage);

        if (newPlayer != null) {
            Player saved = dbService.getPlayerRepository().save(newPlayer);
            if (saved != null) {
                AppState.players.add(saved);
                if (saved.getClubId() != null) {
                    AppState.clubs.stream()
                            .filter(c -> c.getId() != null && c.getId().equals(saved.getClubId()))
                            .findFirst()
                            .ifPresent(c -> c.addPlayer(saved));
                }
                applyFilters();
                System.out.println("✓ Player added: " + saved.getName());
            } else {
                System.err.println("✗ Failed to save player to database: " + newPlayer.getName());
            }
        }
    }

    @FXML
    public void goBack(ActionEvent event) {
        SceneSwitcher.switchTo(event, "main-view.fxml");
    }
}