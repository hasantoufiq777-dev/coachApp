package com.example.coachsapp.controller;

import com.example.coachsapp.model.Player;
import com.example.coachsapp.util.SceneSwitcher;
import com.example.coachsapp.util.AppState;
import com.example.coachsapp.dialog.AddPlayerDialog;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
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
    private TableColumn<Player, String> statusColumn;

    @FXML
    public void initialize() {
        // Use lambda-based factories to avoid reflection issues
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        ageColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getAge()));
        jerseyColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getJersey()));
        positionColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getPosition().toString()));
        statusColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(
                cellData.getValue().isInjured() ? "Injured" : "Available"));

        // Bind the table items to the global observable list
        playerTable.setItems(AppState.players);
    }

    @FXML
    public void addPlayer() {
        Stage stage = (Stage) playerTable.getScene().getWindow();
        AddPlayerDialog dialog = new AddPlayerDialog();
        Player newPlayer = dialog.showDialog(stage);
        
        if (newPlayer != null) {
            AppState.players.add(newPlayer); // add to global observable list
            System.out.println("✓ Player added: " + newPlayer.getName() + " - #" + newPlayer.getJersey() + " (" + newPlayer.getPosition() + ")");
        }
    }

    @FXML
    public void deletePlayer() {
        Player selected = playerTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            AppState.players.remove(selected);
            System.out.println("✓ Deleted: " + selected.getName());
        }
    }

    @FXML
    public void goBack(ActionEvent event) {
        SceneSwitcher.switchTo(event, "main-view.fxml");
    }
}
