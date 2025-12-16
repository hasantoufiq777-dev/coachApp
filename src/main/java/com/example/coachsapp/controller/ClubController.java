package com.example.coachsapp.controller;

import com.example.coachsapp.model.Club;
import com.example.coachsapp.model.Manager;
import com.example.coachsapp.model.Player;
import com.example.coachsapp.util.SceneSwitcher;
import com.example.coachsapp.util.AppState;
import com.example.coachsapp.dialog.AddClubDialog;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.util.Callback;

public class ClubController {

    @FXML
    private TableView<Club> clubTable;

    @FXML
    private TableColumn<Club, Integer> idColumn;

    @FXML
    private TableColumn<Club, String> nameColumn;

    @FXML
    private TableColumn<Club, Integer> playerCountColumn;

    // Managers and Players details
    @FXML
    private ListView<Manager> managerList;

    @FXML
    private TableView<Player> playersTable;

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
    public void initialize() {
        // Set up club table columns
        idColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getClubName()));
        playerCountColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getPlayers().size()).asObject());

        // Bind the club table to the global observable list
        clubTable.setItems(AppState.clubs);

        // Set up manager list cell factory
        managerList.setCellFactory(new Callback<ListView<Manager>, ListCell<Manager>>() {
            @Override
            public ListCell<Manager> call(ListView<Manager> param) {
                return new ListCell<Manager>() {
                    @Override
                    protected void updateItem(Manager item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText("• " + item.getName());
                        }
                    }
                };
            }
        });

        // Set up player table columns
        playerNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        playerAgeColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getAge()).asObject());
        playerJerseyColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getJersey()).asObject());
        playerPositionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPosition().toString()));
        playerStatusColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().isInjured() ? "Injured" : "Available"));

        // Add listener to club table selection
        clubTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateClubDetails(newValue);
            } else {
                clearClubDetails();
            }
        });
    }

    private void updateClubDetails(Club selectedClub) {
        // Update managers list
        var managersForClub = AppState.managers.stream()
            .filter(m -> m.getClub().getId() != null && m.getClub().getId().equals(selectedClub.getId()))
            .toList();

        if (managersForClub.isEmpty()) {
            managerList.setItems(FXCollections.observableArrayList());
        } else {
            managerList.setItems(FXCollections.observableArrayList(managersForClub));
        }

        // Update players list
        playersTable.setItems(FXCollections.observableArrayList(selectedClub.getPlayers()));

        System.out.println("✓ Club Details Loaded: " + selectedClub.getClubName());
        System.out.println("  Managers: " + managersForClub.size());
        System.out.println("  Players: " + selectedClub.getPlayers().size());
    }

    private void clearClubDetails() {
        managerList.setItems(FXCollections.observableArrayList());
        playersTable.setItems(FXCollections.observableArrayList());
    }

    @FXML
    public void addClub() {
        Stage stage = (Stage) clubTable.getScene().getWindow();
        AddClubDialog dialog = new AddClubDialog();
        Club newClub = dialog.showDialog(stage);

        if (newClub != null) {
            // Set club ID (in-memory)
            if (AppState.clubs.isEmpty()) {
                newClub.setId(1);
            } else {
                Integer maxId = AppState.clubs.stream()
                    .map(Club::getId)
                    .filter(id -> id != null)
                    .max(Integer::compare)
                    .orElse(0);
                newClub.setId(maxId + 1);
            }

            AppState.clubs.add(newClub);
            System.out.println("✓ Club added: " + newClub.getClubName() + " (ID: " + newClub.getId() + ")");
        }
    }

    @FXML
    public void deleteClub() {
        Club selected = clubTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            AppState.clubs.remove(selected);
            clearClubDetails();
            System.out.println("✓ Deleted club: " + selected.getClubName());
        } else {
            System.out.println("✗ Please select a club to delete");
        }
    }

    @FXML
    public void goBack(ActionEvent event) {
        SceneSwitcher.switchTo(event, "main-view.fxml");
    }
}

