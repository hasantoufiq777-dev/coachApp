package com.example.coachsapp.controller;

import com.example.coachsapp.db.DatabaseService;
import com.example.coachsapp.db.TransferRequestRepository;
import com.example.coachsapp.model.*;
import com.example.coachsapp.util.AppState;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Alert.AlertType;
import javafx.geometry.Pos;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class TransferRequestController {

    @FXML
    private VBox playerSection;

    @FXML
    private VBox managerSection;

    @FXML
    private VBox adminSection;

    @FXML
    private ComboBox<String> destinationTypeCombo;
    
    @FXML
    private ComboBox<Club> destinationClubCombo;

    @FXML
    private TextArea remarksArea;

    @FXML
    private Label statusLabel;

    @FXML
    private TableView<TransferRequest> requestsTable;

    @FXML
    private TableColumn<TransferRequest, String> playerNameCol;

    @FXML
    private TableColumn<TransferRequest, String> sourceClubCol;

    @FXML
    private TableColumn<TransferRequest, String> destClubCol;

    @FXML
    private TableColumn<TransferRequest, String> statusCol;

    @FXML
    private TableColumn<TransferRequest, String> dateCol;
    
    @FXML
    private TableColumn<TransferRequest, String> feeCol;

    @FXML
    private TableColumn<TransferRequest, Void> actionCol;

    private ObservableList<TransferRequest> transferRequests = FXCollections.observableArrayList();
    private DatabaseService dbService;
    private TransferRequestRepository transferRequestRepo;

    @FXML
    public void initialize() {
        System.out.println("\n========== TRANSFER REQUEST CONTROLLER INITIALIZED ==========");
        User currentUser = AppState.currentUser;
        if (currentUser != null) {
            System.out.println("Current User: " + currentUser.getUsername());
            System.out.println("User Role: " + currentUser.getRole());
            System.out.println("User Club ID: " + currentUser.getClubId());
            System.out.println("User Player ID: " + currentUser.getPlayerId());
        } else {
            System.out.println("ERROR: No current user found!");
        }
        
        dbService = DatabaseService.getInstance();
        transferRequestRepo = dbService.getTransferRequestRepository();

        // Setup table columns
        playerNameCol.setCellValueFactory(new PropertyValueFactory<>("playerName"));
        sourceClubCol.setCellValueFactory(new PropertyValueFactory<>("sourceClubName"));
        destClubCol.setCellValueFactory(new PropertyValueFactory<>("destinationClubName"));
        statusCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus().getDisplayName()));
        dateCol.setCellValueFactory(cellData -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            return new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getRequestDate().format(formatter));
        });
        
        if (feeCol != null) {
            feeCol.setCellValueFactory(cellData -> {
                Double fee = cellData.getValue().getTransferFee();
                String feeStr = fee != null ? String.format("$%.2fM", fee) : "Not Set";
                return new javafx.beans.property.SimpleStringProperty(feeStr);
            });
        }

        // Setup action column based on role
        setupActionColumn();

        requestsTable.setItems(transferRequests);

        // Show appropriate UI based on role
        setupUIForRole();
        loadTransferRequests();
        
        System.out.println("Transfer requests loaded: " + transferRequests.size());
        System.out.println("============================================================\n");
    }

    private void setupUIForRole() {
        User currentUser = AppState.currentUser;
        if (currentUser == null) return;

        // Hide all sections first
        playerSection.setVisible(false);
        playerSection.setManaged(false);
        managerSection.setVisible(false);
        managerSection.setManaged(false);
        adminSection.setVisible(false);
        adminSection.setManaged(false);

        switch (currentUser.getRole()) {
            case PLAYER:
                playerSection.setVisible(true);
                playerSection.setManaged(true);
                setupPlayerSection();
                break;
            case CLUB_MANAGER:
                managerSection.setVisible(true);
                managerSection.setManaged(true);
                break;
            case SYSTEM_ADMIN:
            case CLUB_OWNER:
                adminSection.setVisible(true);
                adminSection.setManaged(true);
                break;
        }
    }

    private void setupPlayerSection() {
        if (destinationTypeCombo != null) {
            destinationTypeCombo.setItems(FXCollections.observableArrayList(
                "General Market", "Specific Club"
            ));
            destinationTypeCombo.setValue("General Market");
            
            // Show/hide club combo based on type
            destinationTypeCombo.setOnAction(e -> {
                String type = destinationTypeCombo.getValue();
                boolean showClubCombo = type != null && type.equals("Specific Club");
                if (destinationClubCombo != null) {
                    destinationClubCombo.setVisible(showClubCombo);
                    destinationClubCombo.setManaged(showClubCombo);
                }
            });
        }
        
        loadClubsForPlayer();
        
        // Initially hide club combo
        if (destinationClubCombo != null) {
            destinationClubCombo.setVisible(false);
            destinationClubCombo.setManaged(false);
        }
    }

    private void loadClubsForPlayer() {
        List<Club> allClubs = dbService.getClubRepository().findAll();
        User currentUser = AppState.currentUser;

        // Filter out player's current club
        List<Club> availableClubs = allClubs.stream()
                .filter(club -> !club.getId().equals(currentUser.getClubId()))
                .toList();

        if (destinationClubCombo != null) {
            destinationClubCombo.setItems(FXCollections.observableArrayList(availableClubs));
        }
    }

    private void loadTransferRequests() {
        System.out.println("\n========== LOADING TRANSFER REQUESTS ==========");
        User currentUser = AppState.currentUser;
        if (currentUser == null) {
            System.out.println("ERROR: Current user is null!");
            return;
        }

        List<TransferRequest> requests;
        switch (currentUser.getRole()) {
            case PLAYER:
                requests = transferRequestRepo.findByPlayerId(currentUser.getPlayerId());
                System.out.println("[PLAYER] Loaded " + requests.size() + " transfer requests");
                break;
            case CLUB_MANAGER:
                // Show only requests from own club (need to set fees or cancel)
                Integer clubId = currentUser.getClubId();
                System.out.println("[MANAGER] Club ID: " + clubId);
                if (clubId == null) {
                    System.out.println("ERROR: Manager's club ID is NULL!");
                    requests = List.of();
                } else {
                    requests = transferRequestRepo.findBySourceClubId(clubId);
                    System.out.println("[MANAGER] Loaded " + requests.size() + " transfer requests for club " + clubId);
                    for (TransferRequest req : requests) {
                        System.out.println("  - ID: " + req.getId() + ", Player: " + req.getPlayerName() + 
                            ", Status: " + req.getStatus() + ", Fee: $" + req.getTransferFee() + "M");
                    }
                }
                break;
            case SYSTEM_ADMIN:
            case CLUB_OWNER:
                requests = transferRequestRepo.findAll();
                System.out.println("[ADMIN] Loaded " + requests.size() + " transfer requests");
                break;
            default:
                requests = List.of();
                System.out.println("Unknown role: " + currentUser.getRole());
        }

        transferRequests.clear();
        transferRequests.addAll(requests);
        
        System.out.println("Transfer requests added to table: " + transferRequests.size());
        System.out.println("Table items count: " + requestsTable.getItems().size());
        System.out.println("================================================\n");
    }

    private void setupActionColumn() {
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button approveBtn = new Button("Approve");
            private final Button cancelBtn = new Button("Cancel");
            private final HBox pendingBox = new HBox(5, approveBtn, cancelBtn);
            private final HBox marketBox = new HBox(5, cancelBtn);

            {
                approveBtn.getStyleClass().addAll("btn", "btn-success");
                cancelBtn.getStyleClass().addAll("btn", "btn-danger");
                
                pendingBox.setAlignment(Pos.CENTER);
                marketBox.setAlignment(Pos.CENTER);
                
                approveBtn.setOnAction(e -> handleApproveAndSetFee(getTableRow().getItem()));
                cancelBtn.setOnAction(e -> handleCancel(getTableRow().getItem()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    TransferRequest request = getTableRow().getItem();
                    User currentUser = AppState.currentUser;

                    if (currentUser != null && currentUser.getRole() == Role.CLUB_MANAGER) {
                        // Manager of source club can approve or cancel
                        if (request.getSourceClubId().equals(currentUser.getClubId())) {
                            System.out.println("[DEBUG] Showing buttons for request - Player: " + request.getPlayerName() + ", Status: " + request.getStatus());
                            if (request.getStatus() == TransferRequest.TransferStatus.PENDING_APPROVAL) {
                                setGraphic(pendingBox);
                            } else if (request.getStatus() == TransferRequest.TransferStatus.IN_MARKET) {
                                // Can still cancel from market
                                setGraphic(marketBox);
                            } else {
                                setGraphic(null);
                            }
                        } else {
                            System.out.println("[DEBUG] Request not from manager's club - Player: " + request.getPlayerName() + ", SourceClub: " + request.getSourceClubId() + ", ManagerClub: " + currentUser.getClubId());
                            setGraphic(null);
                        }
                    } else if (currentUser != null && currentUser.getRole() == Role.SYSTEM_ADMIN) {
                        // Admin can see all but only approve if PENDING_APPROVAL
                        if (request.getStatus() == TransferRequest.TransferStatus.PENDING_APPROVAL) {
                            setGraphic(pendingBox);
                        } else {
                            setGraphic(null);
                        }
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
    }

    @FXML
    public void handleSubmitTransferRequest() {
        System.out.println("\n========== SUBMITTING TRANSFER REQUEST ==========");
        User currentUser = AppState.currentUser;
        if (currentUser == null || currentUser.getPlayerId() == null) {
            System.out.println("ERROR: Current user or player ID is null");
            statusLabel.setText("Error: Player not found");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        
        System.out.println("Player ID: " + currentUser.getPlayerId());
        System.out.println("Player Club ID: " + currentUser.getClubId());

        // Check if player already has pending request
        List<TransferRequest> existing = transferRequestRepo.findByPlayerId(currentUser.getPlayerId());
        System.out.println("Existing requests for player: " + existing.size());
        boolean hasPending = existing.stream()
                .anyMatch(r -> r.getStatus() == TransferRequest.TransferStatus.PENDING_APPROVAL || 
                              r.getStatus() == TransferRequest.TransferStatus.IN_MARKET);
        
        if (hasPending) {
            System.out.println("Player already has a pending request");
            showError("You already have a pending transfer request. Cancel it first before submitting a new one.");
            return;
        }

        String requestType = destinationTypeCombo != null ? destinationTypeCombo.getValue() : null;
        if (requestType == null) {
            statusLabel.setText("Please select request type");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        Integer destinationClubId = null;
        if (requestType.equals("Specific Club")) {
            Club club = destinationClubCombo != null ? destinationClubCombo.getValue() : null;
            if (club == null) {
                statusLabel.setText("Please select a destination club");
                statusLabel.setStyle("-fx-text-fill: red;");
                return;
            }
            destinationClubId = club.getId();
            System.out.println("Target Club ID: " + destinationClubId);
        } else {
            System.out.println("Request type: General Market (no specific club)");
        }

        // Create transfer request
        TransferRequest request = new TransferRequest(
                currentUser.getPlayerId(),
                currentUser.getClubId(),
                destinationClubId  // null for general market
        );
        request.setRemarks(remarksArea != null ? remarksArea.getText() : "");
        request.setStatus(TransferRequest.TransferStatus.PENDING_APPROVAL);
        request.setTransferFee(0.0);
        
        System.out.println("Creating request - Player: " + currentUser.getPlayerId() + ", Source Club: " + currentUser.getClubId() + ", Dest Club: " + destinationClubId);

        TransferRequest saved = transferRequestRepo.save(request);
        if (saved != null && saved.getId() != null) {
            System.out.println("Transfer request saved successfully with ID: " + saved.getId());
        } else {
            System.out.println("ERROR: Failed to save transfer request!");
        }
        System.out.println("====================================================\n");

        statusLabel.setText("Transfer request submitted! Waiting for manager approval.");
        statusLabel.setStyle("-fx-text-fill: green;");

        if (destinationTypeCombo != null) destinationTypeCombo.setValue("General Market");
        if (destinationClubCombo != null) {
            destinationClubCombo.setValue(null);
            destinationClubCombo.setVisible(false);
            destinationClubCombo.setManaged(false);
        }
        if (remarksArea != null) remarksArea.clear();

        loadTransferRequests();
    }

    private void handleApproveAndSetFee(TransferRequest request) {
        if (request == null) return;

        // Show dialog to set transfer fee
        TextInputDialog dialog = new TextInputDialog("5.0");
        dialog.setTitle("Approve Transfer & Set Fee");
        dialog.setHeaderText("Approve transfer request for " + request.getPlayerName());
        dialog.setContentText("Set Transfer Fee (in millions $):");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(feeStr -> {
            try {
                double fee = Double.parseDouble(feeStr);
                if (fee <= 0) {
                    showError("Transfer fee must be greater than 0");
                    return;
                }
                
                // Update request: set fee and put directly in market
                request.setTransferFee(fee);
                request.setStatus(TransferRequest.TransferStatus.IN_MARKET);
                request.setApprovedBySourceDate(LocalDateTime.now());
                transferRequestRepo.save(request);
                
                showSuccess(String.format("Transfer approved! %s is now available in the transfer market for $%.2fM", 
                    request.getPlayerName(), fee));
                loadTransferRequests();
            } catch (NumberFormatException e) {
                showError("Please enter a valid number");
            }
        });
    }

    private void handleCancel(TransferRequest request) {
        if (request == null) return;

        Alert confirmation = new Alert(AlertType.CONFIRMATION);
        confirmation.setTitle("Cancel Transfer Request");
        confirmation.setHeaderText("Cancel transfer request for " + request.getPlayerName() + "?");
        confirmation.setContentText("This action cannot be undone.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                request.setStatus(TransferRequest.TransferStatus.CANCELLED);
                transferRequestRepo.save(request);

                showSuccess("Transfer request cancelled");
                loadTransferRequests();
            }
        });
    }

    private void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void goBack(ActionEvent event) {
        com.example.coachsapp.util.SceneSwitcher.switchTo(event, "main-view.fxml");
    }
}
