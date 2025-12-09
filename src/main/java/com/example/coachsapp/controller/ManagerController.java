package com.example.coachsapp.controller;

import com.example.coachsapp.model.Manager;
import com.example.coachsapp.util.AppState;
import com.example.coachsapp.util.SceneSwitcher;
import com.example.coachsapp.dialog.AddManagerDialog;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.Button;
import javafx.collections.FXCollections;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

public class ManagerController {

    @FXML
    private ListView<Manager> managerList;

    @FXML
    public void initialize() {
        refreshManagerList();
    }

    @FXML
    public void addManager() {
        Stage stage = (Stage) managerList.getScene().getWindow();
        AddManagerDialog dialog = new AddManagerDialog();
        Manager newManager = dialog.showDialog(stage);
        
        if (newManager != null) {
            AppState.managers.add(newManager);
            refreshManagerList();
            System.out.println("✓ Manager added: " + newManager.getName() + " @ " + newManager.getClub().getClubName());
        }
    }

    @FXML
    public void deleteManager() {
        Manager selected = managerList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            AppState.managers.remove(selected);
            refreshManagerList();
            System.out.println("✓ Deleted: " + selected.getName());
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

