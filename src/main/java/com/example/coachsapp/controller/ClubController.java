package com.example.coachsapp.controller;

import com.example.coachsapp.db.DatabaseService;
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
    
    private DatabaseService dbService;

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
        dbService = DatabaseService.getInstance();
        

        idColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getClubName()));
        playerCountColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getPlayers().size()).asObject());


        clubTable.setItems(AppState.clubs);


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


        playerNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        playerAgeColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getAge()).asObject());
        playerJerseyColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getJersey()).asObject());
        playerPositionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPosition().toString()));
        playerStatusColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().isInjured() ? "Injured" : "Available"));


        clubTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateClubDetails(newValue);
            } else {
                clearClubDetails();
            }
        });
    }

    private void updateClubDetails(Club selectedClub) {

        var managersForClub = AppState.managers.stream()
            .filter(m -> m.getClub().getId() != null && m.getClub().getId().equals(selectedClub.getId()))
            .toList();

        if (managersForClub.isEmpty()) {
            managerList.setItems(FXCollections.observableArrayList());
        } else {
            managerList.setItems(FXCollections.observableArrayList(managersForClub));
        }

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

            boolean existsInAppState = AppState.clubs.stream()
                .anyMatch(c -> c.getClubName().equalsIgnoreCase(newClub.getClubName()));
            
            if (existsInAppState) {
                showAlert("Duplicate Club", "A club with the name '" + newClub.getClubName() + "' already exists!");
                return;
            }
            

            Club savedClub = dbService.getClubRepository().save(newClub);
            
            if (savedClub != null) {
                AppState.clubs.add(savedClub);
                System.out.println("✓ Club added: " + savedClub.getClubName() + " (ID: " + savedClub.getId() + ")");
                showAlert("Success", "Club added successfully: " + savedClub.getClubName());
            } else {
                System.err.println("✗ Failed to save club");
                showAlert("Error", "Failed to save club.\n\nThis club name might already exist in the database.\nPlease try a different name.");
            }
        }
    }
    
    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void deleteClub() {
        Club selected = clubTable.getSelectionModel().getSelectedItem();
        if (selected != null) {

            dbService.getClubRepository().delete(selected.getId());

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

