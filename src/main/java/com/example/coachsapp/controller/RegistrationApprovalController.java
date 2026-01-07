package com.example.coachsapp.controller;

import com.example.coachsapp.db.DatabaseService;
import com.example.coachsapp.model.*;
import com.example.coachsapp.util.AppState;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class RegistrationApprovalController {

    @FXML
    private TableView<RegistrationRequest> requestsTable;

    @FXML
    private TableColumn<RegistrationRequest, String> usernameCol;

    @FXML
    private TableColumn<RegistrationRequest, String> roleCol;

    @FXML
    private TableColumn<RegistrationRequest, String> clubCol;

    @FXML
    private TableColumn<RegistrationRequest, String> statusCol;

    @FXML
    private TableColumn<RegistrationRequest, String> dateCol;

    @FXML
    private TableColumn<RegistrationRequest, Void> actionCol;

    @FXML
    private Label pendingCountLabel;

    private ObservableList<RegistrationRequest> requests = FXCollections.observableArrayList();
    private DatabaseService dbService;

    @FXML
    public void initialize() {
        dbService = DatabaseService.getInstance();

        usernameCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getUsername()));
        roleCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getRequestedRole().getDisplayName()));
        clubCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getClubName()));
        statusCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus().getDisplayName()));
        dateCol.setCellValueFactory(cellData -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            return new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getRequestDate().format(formatter));
        });

        setupActionColumn();

        requestsTable.setItems(requests);
        loadRequests();
    }

    private void setupActionColumn() {
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button approveBtn = new Button("Approve");
            private final Button rejectBtn = new Button("Reject");
            private final HBox box = new HBox(10, approveBtn, rejectBtn);

            {
                approveBtn.setOnAction(e -> handleApprove(getTableRow().getItem()));
                rejectBtn.setOnAction(e -> handleReject(getTableRow().getItem()));
                approveBtn.getStyleClass().addAll("btn", "btn-primary");
                rejectBtn.getStyleClass().addAll("btn", "btn-danger");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    RegistrationRequest request = getTableRow().getItem();
                    setGraphic(request.getStatus() == RegistrationRequest.RequestStatus.PENDING ? box : null);
                }
            }
        });
    }

    private void loadRequests() {
        List<RegistrationRequest> allRequests = dbService.getRegistrationRequestRepository().findAll();
        requests.clear();
        requests.addAll(allRequests);

        long pendingCount = allRequests.stream()
            .filter(r -> r.getStatus() == RegistrationRequest.RequestStatus.PENDING)
            .count();
        
        pendingCountLabel.setText("Pending Requests: " + pendingCount);
    }

    private void handleApprove(RegistrationRequest request) {
        if (request == null) return;

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Approve Registration");
        confirmAlert.setHeaderText("Approve registration for: " + request.getUsername());
        confirmAlert.setContentText("This will create a new " + request.getRequestedRole().getDisplayName() + 
                                   " profile for club: " + request.getClubName() + 
                                   "\n\nThe profile details (age, jersey, etc.) can be edited later by the manager.");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (request.getRequestedRole() == Role.CLUB_MANAGER) {
                approveManager(request);
            } else if (request.getRequestedRole() == Role.PLAYER) {
                approvePlayer(request);
            }
        }
    }

    private void approveManager(RegistrationRequest request) {
        Club club = dbService.getClubRepository().findById(request.getClubId());
        if (club == null) {
            showAlert("Error", "Club not found!");
            return;
        }

        Manager newManager = new Manager(request.getUsername(), club);
        if (request.getAge() != null) {
            newManager.setAge(request.getAge());
        }
        Manager savedManager = dbService.getManagerRepository().save(newManager);
        
        if (savedManager != null) {
            User newUser = new User(null, request.getUsername(), request.getPassword(), 
                                   Role.CLUB_MANAGER, request.getClubId(), null, savedManager.getId());
            dbService.getUserRepository().save(newUser);

            request.setStatus(RegistrationRequest.RequestStatus.APPROVED);
            request.setApprovedDate(java.time.LocalDateTime.now());
            dbService.getRegistrationRequestRepository().save(request);

            AppState.managers.add(savedManager);

            showAlert("Approved", "Manager registration approved!\nUsername: " + request.getUsername());
            loadRequests();
        } else {
            showAlert("Error", "Failed to create manager profile!");
        }
    }

    private void approvePlayer(RegistrationRequest request) {
        Club club = dbService.getClubRepository().findById(request.getClubId());
        if (club == null) {
            showAlert("Error", "Club not found!");
            return;
        }

        int age = request.getAge() != null ? request.getAge() : 25;
        int defaultJersey = (int) (Math.random() * 99) + 1;
        
        Position position = Position.MIDFIELDER;
        if (request.getPosition() != null && !request.getPosition().isEmpty()) {
            try {
                position = Position.valueOf(request.getPosition());
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid position in request: " + request.getPosition() + ", using default MIDFIELDER");
            }
        }

        Player newPlayer = new Player(request.getUsername(), age, defaultJersey, position);
        newPlayer.setClubId(club.getId());
        Player savedPlayer = dbService.getPlayerRepository().save(newPlayer);
        
        if (savedPlayer != null) {
            User newUser = new User(null, request.getUsername(), request.getPassword(), 
                                   Role.PLAYER, request.getClubId(), savedPlayer.getId(), null);
            dbService.getUserRepository().save(newUser);

            request.setStatus(RegistrationRequest.RequestStatus.APPROVED);
            request.setApprovedDate(java.time.LocalDateTime.now());
            dbService.getRegistrationRequestRepository().save(request);

            AppState.players.add(savedPlayer);

            showAlert("Approved", "Player registration approved!\nUsername: " + request.getUsername() + 
                                 "\nAge: " + age + "\nPosition: " + position +
                                 "\n\nThe manager can edit jersey number and other details.");
            loadRequests();
        } else {
            showAlert("Error", "Failed to create player profile!");
        }
    }

    private void handleReject(RegistrationRequest request) {
        if (request == null) return;

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reject Registration");
        dialog.setHeaderText("Reject registration for: " + request.getFullName());
        dialog.setContentText("Reason (optional):");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(reason -> {
            request.setStatus(RegistrationRequest.RequestStatus.REJECTED);
            request.setRemarks(reason);
            dbService.getRegistrationRequestRepository().save(request);

            showAlert("Rejected", "Registration has been rejected.");
            loadRequests();
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void goBack(ActionEvent event) {
        com.example.coachsapp.util.SceneSwitcher.switchTo(event, "main-view.fxml");
    }
}
