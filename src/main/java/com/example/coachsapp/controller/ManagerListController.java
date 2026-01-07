package com.example.coachsapp.controller;

import com.example.coachsapp.model.Manager;
import com.example.coachsapp.model.User;
import com.example.coachsapp.model.Role;
import com.example.coachsapp.model.Club;
import com.example.coachsapp.util.SceneSwitcher;
import com.example.coachsapp.util.AppState;
import com.example.coachsapp.dialog.AddManagerDialog;
import com.example.coachsapp.db.ManagerRepository;
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

public class ManagerListController {

    @FXML
    private FlowPane managerCardsPane;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private ComboBox<String> clubFilterCombo;

    private DatabaseService dbService;
    private java.util.List<Manager> baseManagerList;

    @FXML
    public void initialize() {
        dbService = DatabaseService.getInstance();
        System.out.println("✓ ManagerListController initialized with " + AppState.managers.size() + " managers");
        setupFilters();
        loadBaseManagers();
        applyFilters();
    }

    private void setupFilters() {
        clubFilterCombo.setOnAction(e -> applyFilters());
    }

    private void loadBaseManagers() {
        User currentUser = AppState.currentUser;
        
        if (currentUser == null) {
            baseManagerList = new java.util.ArrayList<>();
            System.out.println("⚠ No user logged in");
            return;
        }

        // All roles see all managers (typically only admin accesses this)
        baseManagerList = new java.util.ArrayList<>(AppState.managers);
        System.out.println("✓ Card view: base list has " + baseManagerList.size() + " managers");
        
        // Populate club filter with all clubs
        ObservableList<String> clubs = FXCollections.observableArrayList("All Clubs");
        clubs.addAll(AppState.clubs.stream()
            .map(Club::getClubName)
            .sorted()
            .collect(Collectors.toList()));
        clubFilterCombo.setItems(clubs);
        clubFilterCombo.setValue("All Clubs");
    }

    private void applyFilters() {
        if (baseManagerList == null) {
            return;
        }

        String selectedClub = clubFilterCombo.getValue();
        
        java.util.List<Manager> filteredManagers = new java.util.ArrayList<>(baseManagerList);

        // Apply club filter
        if (selectedClub != null && !selectedClub.equals("All Clubs")) {
            filteredManagers = filteredManagers.stream()
                .filter(m -> {
                    Club club = m.getClub();
                    return club != null && club.getClubName().equals(selectedClub);
                })
                .collect(Collectors.toList());
        }

        createManagerCards(filteredManagers);
        System.out.println("✓ Card filters applied: showing " + filteredManagers.size() + " manager cards (Club: " + selectedClub + ")");
    }

    @FXML
    public void clearFilters() {
        clubFilterCombo.setValue("All Clubs");
        applyFilters();
    }

    private void createManagerCards(java.util.List<Manager> managersToShow) {
        managerCardsPane.getChildren().clear();
        
        for (Manager manager : managersToShow) {
            VBox card = createManagerCard(manager);
            managerCardsPane.getChildren().add(card);
        }
    }

    private VBox createManagerCard(Manager manager) {
        VBox card = new VBox(10);
        card.setPrefSize(240, 280);
        card.setMaxSize(240, 280);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(20));
        
        String cardStyle = "-fx-background-color: linear-gradient(to bottom, #1a1a1a 0%, #2d2d2d 100%);" +
                          "-fx-border-color: #3498db;" +
                          "-fx-border-width: 3;" +
                          "-fx-border-radius: 10;" +
                          "-fx-background-radius: 10;" +
                          "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 10, 0, 0, 4);";
        card.setStyle(cardStyle);
        
        // Manager icon -        Label iconLabel = new Label("");
  Label iconLabel = new Label("👔");
        iconLabel.setStyle("-fx-font-size: 60px; -fx-text-fill: white;");
        
        // Manager name
        Label nameLabel = new Label(manager.getName().toUpperCase());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(200);
        nameLabel.setAlignment(Pos.CENTER);
        

        VBox statsBox = new VBox(5);
        statsBox.setAlignment(Pos.CENTER_LEFT);
        statsBox.setPadding(new Insets(10));
        statsBox.setStyle("-fx-background-color: rgba(0,0,0,0.3); -fx-padding: 10; -fx-background-radius: 5;");

        Label idLabel = new Label("ID: " + (manager.getId() != null ? manager.getId() : "N/A"));
        idLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #aaa;");
        

        Label ageLabel = new Label("AGE: " + (manager.getAge() != null ? manager.getAge() : "N/A"));
        ageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #aaa;");
        

        String clubName = manager.getClub() != null ? manager.getClub().getClubName() : "No Club";
        Label clubLabel = new Label("CLUB: " + clubName);
        clubLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ffdd57; -fx-font-weight: bold;");
        clubLabel.setWrapText(true);
        clubLabel.setMaxWidth(180);
        
        statsBox.getChildren().addAll(idLabel, ageLabel, clubLabel);
        

        HBox buttonBox = new HBox(8);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(8, 0, 0, 0));
        
        Button viewBtn = new Button("View");
        viewBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 15;");
        viewBtn.setOnAction(e -> viewManagerProfile(manager));
        
        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 15;");
        deleteBtn.setOnAction(e -> deleteManager(manager));
        
        buttonBox.getChildren().addAll(viewBtn, deleteBtn);
        
        card.getChildren().addAll(iconLabel, nameLabel, statsBox, buttonBox);
        
        card.setOnMouseEntered(e -> {
            card.setStyle(cardStyle + "-fx-cursor: hand; -fx-scale-x: 1.05; -fx-scale-y: 1.05;");
        });
        card.setOnMouseExited(e -> {
            card.setStyle(cardStyle);
        });
        
        return card;
    }

    private void viewManagerProfile(Manager manager) {
        AppState.setSelectedManager(manager);
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/example/coachsapp/manager-profile-view.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load(), 800, 600);
            javafx.stage.Stage stage = (javafx.stage.Stage) managerCardsPane.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
            System.out.println("✓ Opening profile for: " + manager.getName());
        } catch (Exception e) {
            System.err.println("Error loading manager profile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void deleteManager(Manager manager) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Manager");
        confirm.setContentText("Are you sure you want to delete " + manager.getName() + "?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (manager.getId() != null) {
                    ManagerRepository managerRepo = dbService.getManagerRepository();
                    boolean deleted = managerRepo.delete(manager.getId());
                    if (deleted) {
                        AppState.managers.remove(manager);
                        applyFilters();
                        System.out.println("✓ Manager deleted: " + manager.getName());
                    } else {
                        System.err.println("✗ Failed to delete manager: " + manager.getName());
                    }
                }
            }
        });
    }

    @FXML
    public void addManager() {
        Stage stage = (Stage) managerCardsPane.getScene().getWindow();
        AddManagerDialog dialog = new AddManagerDialog();
        Manager newManager = dialog.showDialog(stage);

        if (newManager != null) {
            ManagerRepository managerRepo = dbService.getManagerRepository();
            Manager saved = managerRepo.save(newManager);
            if (saved != null) {
                AppState.managers.add(saved);
                
                Club club = saved.getClub();
                if (club != null && !AppState.clubs.stream().anyMatch(c -> c.getId() != null && c.getId().equals(club.getId()))) {
                    AppState.clubs.add(club);
                }
                
                applyFilters();
                System.out.println("✓ Manager added: " + saved.getName() + " @ " + club.getClubName());
            } else {
                System.err.println("✗ Failed to save manager to database: " + newManager.getName());
            }
        }
    }

    @FXML
    public void goBack(ActionEvent event) {
        SceneSwitcher.switchTo(event, "main-view.fxml");
    }
}
