package com.example.coachsapp.controller;

import com.example.coachsapp.db.DatabaseService;
import com.example.coachsapp.model.*;
import com.example.coachsapp.util.AppState;
import com.example.coachsapp.util.SceneSwitcher;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class TransferMarketController {

    @FXML
    private VBox root;

    @FXML
    private ComboBox<String> positionFilter;

    @FXML
    private FlowPane playersFlowPane;

    @FXML
    private Label resultCountLabel;

    @FXML
    private Button backButton;

    private DatabaseService dbService;
    private List<TransferRequest> allMarketRequests;

    @FXML
    public void initialize() {
        dbService = DatabaseService.getInstance();

        positionFilter.setItems(FXCollections.observableArrayList(
            "All Positions", "GOALKEEPER", "DEFENDER", "MIDFIELDER", "FORWARD"
        ));
        positionFilter.setValue("All Positions");
        
        loadMarketPlayers();
    }

    private void loadMarketPlayers() {
        playersFlowPane.getChildren().clear();
        

        allMarketRequests = dbService.getTransferRequestRepository().findInMarket();
        
        updateResultCount(allMarketRequests.size());
        
        for (TransferRequest request : allMarketRequests) {
            Player player = dbService.getPlayerRepository().findById(request.getPlayerId());
            if (player != null) {
                VBox card = createPlayerCard(player, request);
                playersFlowPane.getChildren().add(card);
            }
        }
    }

    @FXML
    public void filterByPosition(ActionEvent event) {
        playersFlowPane.getChildren().clear();
        
        String selectedPosition = positionFilter.getValue();
        List<TransferRequest> filteredRequests;
        
        if (selectedPosition == null || selectedPosition.equals("All Positions")) {
            filteredRequests = allMarketRequests;
        } else {
            Position posFilter = Position.valueOf(selectedPosition);
            filteredRequests = allMarketRequests.stream()
                .filter(req -> {
                    Player p = dbService.getPlayerRepository().findById(req.getPlayerId());
                    return p != null && p.getPosition() == posFilter;
                })
                .collect(Collectors.toList());
        }
        
        updateResultCount(filteredRequests.size());
        
        for (TransferRequest request : filteredRequests) {
            Player player = dbService.getPlayerRepository().findById(request.getPlayerId());
            if (player != null) {
                VBox card = createPlayerCard(player, request);
                playersFlowPane.getChildren().add(card);
            }
        }
    }

    private VBox createPlayerCard(Player player, TransferRequest request) {
        VBox card = new VBox(10);
        card.setPrefWidth(220);
        card.setPrefHeight(340);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(15));
        card.getStyleClass().add("player-card");
        

        String gradient = switch (player.getPosition()) {
            case GOALKEEPER -> "linear-gradient(to bottom, #FF6B6B, #8B0000)";
            case DEFENDER -> "linear-gradient(to bottom, #4ECDC4, #006064)";
            case MIDFIELDER -> "linear-gradient(to bottom, #95E1D3, #004D40)";
            case FORWARD -> "linear-gradient(to bottom, #F38181, #B71C1C)";
        };
        card.setStyle("-fx-background-color: " + gradient + "; " +
                     "-fx-background-radius: 15; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 5);");
        

        Label jerseyLabel = new Label("#" + player.getJersey());
        jerseyLabel.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        jerseyLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.9);");
        

        Label nameLabel = new Label(player.getName());
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        nameLabel.setStyle("-fx-text-fill: white;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(190);
        nameLabel.setAlignment(Pos.CENTER);
        

        Label positionLabel = new Label(player.getPosition().name());
        positionLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        positionLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.8);");
        

        Label ageLabel = new Label("Age: " + player.getAge());
        ageLabel.setFont(Font.font("Arial", 12));
        ageLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.9);");
        

        Label clubLabel = new Label(request.getSourceClubName());
        clubLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        clubLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.9);");
        clubLabel.setWrapText(true);
        clubLabel.setMaxWidth(190);
        clubLabel.setAlignment(Pos.CENTER);
        

        Label feeLabel = new Label(String.format("Transfer Fee: $%.2fM", request.getTransferFee()));
        feeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        feeLabel.setStyle("-fx-text-fill: #FFD700; -fx-background-color: rgba(0,0,0,0.3); " +
                         "-fx-padding: 5 10; -fx-background-radius: 5;");

        Label statusLabel = new Label(player.isInjured() ? "⚠ INJURED" : "✓ AVAILABLE");
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        statusLabel.setStyle(player.isInjured() ? 
            "-fx-text-fill: #FF4444; -fx-background-color: rgba(255,68,68,0.2);" :
            "-fx-text-fill: #44FF44; -fx-background-color: rgba(68,255,68,0.2);"
        );
        statusLabel.setPadding(new Insets(3, 8, 3, 8));
        statusLabel.setStyle(statusLabel.getStyle() + " -fx-background-radius: 10;");
        
        card.getChildren().addAll(jerseyLabel, nameLabel, positionLabel, ageLabel, clubLabel, feeLabel, statusLabel);
        

        User currentUser = AppState.currentUser;
        if (currentUser != null && currentUser.getRole() == Role.CLUB_MANAGER) {
            if (!request.getSourceClubId().equals(currentUser.getClubId())) {
                Button purchaseBtn = new Button("Purchase Player");
                purchaseBtn.getStyleClass().addAll("btn", "btn-primary");
                purchaseBtn.setPrefWidth(180);
                purchaseBtn.setOnAction(e -> purchasePlayer(player, request));
                card.getChildren().add(purchaseBtn);
            } else {
                Label ownPlayerLabel = new Label("Your Club Player");
                ownPlayerLabel.setStyle("-fx-text-fill: #FFA500; -fx-font-weight: bold;");
                card.getChildren().add(ownPlayerLabel);
            }
        }
        
        return card;
    }

    private void purchasePlayer(Player player, TransferRequest request) {
        User currentUser = AppState.currentUser;
        if (currentUser == null || currentUser.getClubId() == null) {
            showError("You must be logged in as a manager to purchase players");
            return;
        }
        

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Purchase");
        confirmation.setHeaderText("Purchase " + player.getName() + "?");
        confirmation.setContentText(String.format(
            "Transfer Fee: $%.2fM\n" +
            "From: %s\n" +
            "To: Your Club\n\n" +
            "Are you sure you want to complete this transfer?",
            request.getTransferFee(),
            request.getSourceClubName()
        ));
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {

                Integer oldClubId = player.getClubId();
                player.setClubId(currentUser.getClubId());
                dbService.getPlayerRepository().update(player);
                

                request.setStatus(TransferRequest.TransferStatus.COMPLETED);
                request.setDestinationClubId(currentUser.getClubId());
                request.setCompletedDate(LocalDateTime.now());
                dbService.getTransferRequestRepository().save(request);

                User playerUser = dbService.getUserRepository().findByPlayerId(player.getId());
                if (playerUser != null) {
                    playerUser.setClubId(currentUser.getClubId());
                    dbService.getUserRepository().save(playerUser);
                    System.out.println("✓ Updated user club_id for player: " + player.getName() + " from club " + oldClubId + " to " + currentUser.getClubId());
                    

                    if (AppState.currentUser != null && AppState.currentUser.getPlayerId() != null && 
                        AppState.currentUser.getPlayerId().equals(player.getId())) {
                        AppState.currentUser.setClubId(currentUser.getClubId());
                        System.out.println("✓ Updated current user's club_id in AppState");
                    }
                }
                

                AppState.refreshPlayers();
                
                showSuccess("Transfer completed successfully!");
                loadMarketPlayers();
            }
        });
    }

    private void updateResultCount(int count) {
        resultCountLabel.setText(count + " player" + (count != 1 ? "s" : "") + " available");
    }

    @FXML
    public void goBack(ActionEvent event) {
        SceneSwitcher.switchTo(event, "main-view.fxml");
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
