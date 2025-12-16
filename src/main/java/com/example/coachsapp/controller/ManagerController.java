package com.example.coachsapp.controller;

import com.example.coachsapp.model.Manager;
import com.example.coachsapp.model.Club;
import com.example.coachsapp.util.AppState;
import com.example.coachsapp.util.SceneSwitcher;
import com.example.coachsapp.dialog.AddManagerDialog;
import com.example.coachsapp.db.ManagerRepository;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.Button;
import javafx.collections.FXCollections;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

public class ManagerController {

    @FXML
    private ListView<Manager> managerList;

    private ManagerRepository managerRepository = new ManagerRepository();

    @FXML
    public void initialize() {
        refreshManagerList();
        
        // Add double-click handler to view manager profile
        managerList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Manager selected = managerList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    viewManagerProfile(selected);
                }
            }
        });
    }

    @FXML
    public void addManager() {
        Stage stage = (Stage) managerList.getScene().getWindow();
        AddManagerDialog dialog = new AddManagerDialog();
        Manager newManager = dialog.showDialog(stage);
        
        if (newManager != null) {
            // Persist the manager (and its club) to the database
            Manager saved = managerRepository.save(newManager);

            if (saved != null) {
                // Ensure AppState holds the saved manager and club (with IDs)
                AppState.managers.add(saved);

                Club club = saved.getClub();
                if (club != null && !AppState.clubs.stream().anyMatch(c -> c.getId() != null && c.getId().equals(club.getId()))) {
                    AppState.clubs.add(club);
                }

                refreshManagerList();
                System.out.println("✓ Manager saved and added: " + saved.getName() + " @ " + club.getClubName());
            } else {
                System.err.println("✗ Failed to save manager to database: " + newManager.getName());
            }
        }
    }

    @FXML
    public void deleteManager() {
        Manager selected = managerList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            AppState.managers.remove(selected);

            // Delete from database if the manager has an ID (was saved to database)
            if (selected.getId() != null) {
                boolean deleted = managerRepository.delete(selected.getId());
                if (deleted) {
                    System.out.println("✓ Manager deleted from database: " + selected.getName());
                } else {
                    System.out.println("✗ Failed to delete manager from database: " + selected.getName());
                }
            }

            refreshManagerList();
            System.out.println("✓ Deleted from app: " + selected.getName());
        }
    }


    @FXML
    public void viewProfile(ActionEvent event) {
        Manager selected = managerList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            AppState.setSelectedManager(selected);
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/coachsapp/manager-profile-view.fxml"));
                javafx.scene.Scene scene = new javafx.scene.Scene(loader.load(), 1000, 700);
                javafx.stage.Stage stage = (javafx.stage.Stage) managerList.getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } catch (Exception e) {
                System.err.println("Error loading manager profile: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("Please select a manager to view profile");
        }
    }

    private void viewManagerProfile(Manager manager) {
        AppState.setSelectedManager(manager);
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/coachsapp/manager-profile-view.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load(), 1000, 700);
            javafx.stage.Stage stage = (javafx.stage.Stage) managerList.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            System.err.println("Error loading manager profile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void goBack(ActionEvent event) {
        SceneSwitcher.switchTo(event, "main-view.fxml");
    }

    private void refreshManagerList() {
        managerList.setItems(FXCollections.observableArrayList(AppState.managers));
    }
}

