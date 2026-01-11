package com.example.coachsapp.controller;

import com.example.coachsapp.db.GamePlanRepository;
import com.example.coachsapp.db.PlayerRepository;
import com.example.coachsapp.model.GamePlan;
import com.example.coachsapp.model.Manager;
import com.example.coachsapp.model.Player;
import com.example.coachsapp.model.Position;
import com.example.coachsapp.util.AppState;
import com.example.coachsapp.util.SceneSwitcher;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller for managing game plans
 * Allows managers to create lineups with 1 GK, 2 DEF, 2 MID, 1 FWD
 */
public class GamePlanController {

    @FXML
    private ListView<GamePlan> gamePlanList;

    @FXML
    private TextField gamePlanNameField;

    @FXML
    private ComboBox<Player> goalkeeperCombo;

    @FXML
    private ComboBox<Player> defender1Combo;

    @FXML
    private ComboBox<Player> defender2Combo;

    @FXML
    private ComboBox<Player> midfielder1Combo;

    @FXML
    private ComboBox<Player> midfielder2Combo;

    @FXML
    private ComboBox<Player> forwardCombo;

    @FXML
    private Button saveButton;

    @FXML
    private Button newButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Label statusLabel;

    @FXML
    private VBox formContainer;

    // Field View Labels
    @FXML
    private Label gkJersey;
    @FXML
    private Label gkName;
    @FXML
    private Label def1Jersey;
    @FXML
    private Label def1Name;
    @FXML
    private Label def2Jersey;
    @FXML
    private Label def2Name;
    @FXML
    private Label mid1Jersey;
    @FXML
    private Label mid1Name;
    @FXML
    private Label mid2Jersey;
    @FXML
    private Label mid2Name;
    @FXML
    private Label fwdJersey;
    @FXML
    private Label fwdName;

    private GamePlanRepository gamePlanRepository;
    private PlayerRepository playerRepository;
    private Manager currentManager;
    private GamePlan currentGamePlan;
    private List<Player> clubPlayers;

    @FXML
    public void initialize() {
        gamePlanRepository = new GamePlanRepository();
        playerRepository = new PlayerRepository();

        // Get the current manager from AppState
        currentManager = AppState.getSelectedManager();
        
        if (currentManager == null || currentManager.getClub() == null) {
            showError("No manager or club selected!");
            return;
        }

        // Load club players
        loadClubPlayers();

        // Populate position-specific combo boxes
        populateComboBoxes();

        // Load existing game plans
        loadGamePlans();

        // Set up list selection listener
        gamePlanList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                GamePlan selected = gamePlanList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    loadGamePlanToForm(selected);
                }
            }
        });

        // Add listeners to combo boxes to update field view in real-time
        goalkeeperCombo.setOnAction(e -> updateFieldView());
        defender1Combo.setOnAction(e -> updateFieldView());
        defender2Combo.setOnAction(e -> updateFieldView());
        midfielder1Combo.setOnAction(e -> updateFieldView());
        midfielder2Combo.setOnAction(e -> updateFieldView());
        forwardCombo.setOnAction(e -> updateFieldView());

        // Initially disable form
        setFormEnabled(false);
    }

    /**
     * Load all players from the manager's club
     */
    private void loadClubPlayers() {
        if (currentManager.getClub().getId() != null) {
            clubPlayers = playerRepository.findByClubId(currentManager.getClub().getId());
            
            // Filter out injured players for selection
            clubPlayers = clubPlayers.stream()
                    .filter(p -> !p.isInjured())
                    .collect(Collectors.toList());
            
            System.out.println("✓ Loaded " + clubPlayers.size() + " available players from " + 
                             currentManager.getClub().getClubName());
        } else {
            clubPlayers = FXCollections.observableArrayList();
            showError("Club has no ID - cannot load players");
        }
    }

    /**
     * Populate combo boxes with position-specific players
     */
    private void populateComboBoxes() {
        // Goalkeepers
        List<Player> goalkeepers = clubPlayers.stream()
                .filter(p -> p.getPosition() == Position.GOALKEEPER)
                .collect(Collectors.toList());
        goalkeeperCombo.setItems(FXCollections.observableArrayList(goalkeepers));

        // Defenders
        List<Player> defenders = clubPlayers.stream()
                .filter(p -> p.getPosition() == Position.DEFENDER)
                .collect(Collectors.toList());
        defender1Combo.setItems(FXCollections.observableArrayList(defenders));
        defender2Combo.setItems(FXCollections.observableArrayList(defenders));

        // Midfielders
        List<Player> midfielders = clubPlayers.stream()
                .filter(p -> p.getPosition() == Position.MIDFIELDER)
                .collect(Collectors.toList());
        midfielder1Combo.setItems(FXCollections.observableArrayList(midfielders));
        midfielder2Combo.setItems(FXCollections.observableArrayList(midfielders));

        // Forwards
        List<Player> forwards = clubPlayers.stream()
                .filter(p -> p.getPosition() == Position.FORWARD)
                .collect(Collectors.toList());
        forwardCombo.setItems(FXCollections.observableArrayList(forwards));

        // Set custom cell factories for better display
        setCellFactory(goalkeeperCombo);
        setCellFactory(defender1Combo);
        setCellFactory(defender2Combo);
        setCellFactory(midfielder1Combo);
        setCellFactory(midfielder2Combo);
        setCellFactory(forwardCombo);
    }

    /**
     * Set custom cell factory for combo box to display player info
     */
    private void setCellFactory(ComboBox<Player> combo) {
        combo.setCellFactory(param -> new ListCell<Player>() {
            @Override
            protected void updateItem(Player player, boolean empty) {
                super.updateItem(player, empty);
                if (empty || player == null) {
                    setText(null);
                } else {
                    setText(String.format("#%d - %s (Age: %d)", 
                           player.getJersey(), player.getName(), player.getAge()));
                }
            }
        });
        combo.setButtonCell(new ListCell<Player>() {
            @Override
            protected void updateItem(Player player, boolean empty) {
                super.updateItem(player, empty);
                if (empty || player == null) {
                    setText(null);
                } else {
                    setText(String.format("#%d - %s", player.getJersey(), player.getName()));
                }
            }
        });
    }

    /**
     * Load all game plans for the current manager
     */
    private void loadGamePlans() {
        if (currentManager.getId() != null) {
            List<GamePlan> gamePlans = gamePlanRepository.findByManagerId(currentManager.getId());
            gamePlanList.setItems(FXCollections.observableArrayList(gamePlans));
            System.out.println("✓ Loaded " + gamePlans.size() + " game plans");
        }
    }

    /**
     * Create a new game plan
     */
    @FXML
    public void createNew() {
        currentGamePlan = new GamePlan(
                currentManager.getId(),
                currentManager.getClub().getId(),
                "New Game Plan"
        );
        
        gamePlanNameField.setText("New Game Plan");
        clearSelections();
        setFormEnabled(true);
        statusLabel.setText("Creating new game plan...");
        statusLabel.setStyle("-fx-text-fill: blue;");
    }

    /**
     * Save the current game plan
     */
    @FXML
    public void saveGamePlan() {
        if (currentGamePlan == null) {
            showError("No game plan selected. Click 'New Game Plan' first.");
            return;
        }

        // Validate name
        String name = gamePlanNameField.getText();
        if (name == null || name.trim().isEmpty()) {
            showError("Please enter a game plan name!");
            return;
        }
        currentGamePlan.setName(name.trim());

        // Get selected players
        currentGamePlan.setGoalkeeper(goalkeeperCombo.getValue());
        currentGamePlan.setDefender1(defender1Combo.getValue());
        currentGamePlan.setDefender2(defender2Combo.getValue());
        currentGamePlan.setMidfielder1(midfielder1Combo.getValue());
        currentGamePlan.setMidfielder2(midfielder2Combo.getValue());
        currentGamePlan.setForward(forwardCombo.getValue());

        // Validate selections
        if (!validateSelections()) {
            return;
        }

        // Save to database
        GamePlan saved = gamePlanRepository.save(currentGamePlan);
        if (saved != null) {
            statusLabel.setText("✓ Game plan saved successfully!");
            statusLabel.setStyle("-fx-text-fill: green;");
            loadGamePlans();
            currentGamePlan = saved;
        } else {
            showError("Failed to save game plan!");
        }
    }

    /**
     * Validate player selections
     */
    private boolean validateSelections() {
        // Check if all positions are filled
        if (currentGamePlan.getGoalkeeperId() == null) {
            showError("Please select a goalkeeper!");
            return false;
        }
        if (currentGamePlan.getDefender1Id() == null) {
            showError("Please select Defender 1!");
            return false;
        }
        if (currentGamePlan.getDefender2Id() == null) {
            showError("Please select Defender 2!");
            return false;
        }
        if (currentGamePlan.getMidfielder1Id() == null) {
            showError("Please select Midfielder 1!");
            return false;
        }
        if (currentGamePlan.getMidfielder2Id() == null) {
            showError("Please select Midfielder 2!");
            return false;
        }
        if (currentGamePlan.getForwardId() == null) {
            showError("Please select a forward!");
            return false;
        }

        // Check for duplicate players
        List<Integer> playerIds = List.of(
                currentGamePlan.getGoalkeeperId(),
                currentGamePlan.getDefender1Id(),
                currentGamePlan.getDefender2Id(),
                currentGamePlan.getMidfielder1Id(),
                currentGamePlan.getMidfielder2Id(),
                currentGamePlan.getForwardId()
        );

        long uniqueCount = playerIds.stream().distinct().count();
        if (uniqueCount != 6) {
            showError("Each player can only be selected once!");
            return false;
        }

        return true;
    }

    /**
     * Delete the selected game plan
     */
    @FXML
    public void deleteGamePlan() {
        GamePlan selected = gamePlanList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a game plan to delete!");
            return;
        }

        // Confirm deletion
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Game Plan");
        alert.setHeaderText("Delete " + selected.getName() + "?");
        alert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean deleted = gamePlanRepository.delete(selected.getId());
            if (deleted) {
                statusLabel.setText("✓ Game plan deleted!");
                statusLabel.setStyle("-fx-text-fill: green;");
                loadGamePlans();
                clearForm();
            } else {
                showError("Failed to delete game plan!");
            }
        }
    }

    /**
     * Load a game plan into the form
     */
    private void loadGamePlanToForm(GamePlan gamePlan) {
        currentGamePlan = gamePlan;
        gamePlanNameField.setText(gamePlan.getName());

        // Set selected players in combo boxes
        goalkeeperCombo.setValue(gamePlan.getGoalkeeper());
        defender1Combo.setValue(gamePlan.getDefender1());
        defender2Combo.setValue(gamePlan.getDefender2());
        midfielder1Combo.setValue(gamePlan.getMidfielder1());
        midfielder2Combo.setValue(gamePlan.getMidfielder2());
        forwardCombo.setValue(gamePlan.getForward());

        setFormEnabled(true);
        statusLabel.setText("Viewing: " + gamePlan.getName());
        statusLabel.setStyle("-fx-text-fill: blue;");
    }

    /**
     * Clear the form
     */
    private void clearForm() {
        currentGamePlan = null;
        gamePlanNameField.clear();
        clearSelections();
        setFormEnabled(false);
        statusLabel.setText("");
    }

    /**
     * Clear all combo box selections
     */
    private void clearSelections() {
        goalkeeperCombo.setValue(null);
        defender1Combo.setValue(null);
        defender2Combo.setValue(null);
        midfielder1Combo.setValue(null);
        midfielder2Combo.setValue(null);
        forwardCombo.setValue(null);
        updateFieldView();
    }

    /**
     * Update the visual field view with current player selections
     */
    private void updateFieldView() {
        // Update Goalkeeper
        Player gk = goalkeeperCombo.getValue();
        if (gk != null) {
            gkJersey.setText("#" + gk.getJersey());
            gkName.setText(gk.getName().length() > 10 ? 
                          gk.getName().substring(0, 10) : gk.getName());
        } else {
            gkJersey.setText("-");
            gkName.setText("GK");
        }

        // Update Defender 1
        Player def1 = defender1Combo.getValue();
        if (def1 != null) {
            def1Jersey.setText("#" + def1.getJersey());
            def1Name.setText(def1.getName().length() > 10 ? 
                            def1.getName().substring(0, 10) : def1.getName());
        } else {
            def1Jersey.setText("-");
            def1Name.setText("DEF");
        }

        // Update Defender 2
        Player def2 = defender2Combo.getValue();
        if (def2 != null) {
            def2Jersey.setText("#" + def2.getJersey());
            def2Name.setText(def2.getName().length() > 10 ? 
                            def2.getName().substring(0, 10) : def2.getName());
        } else {
            def2Jersey.setText("-");
            def2Name.setText("DEF");
        }

        // Update Midfielder 1
        Player mid1 = midfielder1Combo.getValue();
        if (mid1 != null) {
            mid1Jersey.setText("#" + mid1.getJersey());
            mid1Name.setText(mid1.getName().length() > 10 ? 
                            mid1.getName().substring(0, 10) : mid1.getName());
        } else {
            mid1Jersey.setText("-");
            mid1Name.setText("MID");
        }

        // Update Midfielder 2
        Player mid2 = midfielder2Combo.getValue();
        if (mid2 != null) {
            mid2Jersey.setText("#" + mid2.getJersey());
            mid2Name.setText(mid2.getName().length() > 10 ? 
                            mid2.getName().substring(0, 10) : mid2.getName());
        } else {
            mid2Jersey.setText("-");
            mid2Name.setText("MID");
        }

        // Update Forward
        Player fwd = forwardCombo.getValue();
        if (fwd != null) {
            fwdJersey.setText("#" + fwd.getJersey());
            fwdName.setText(fwd.getName().length() > 10 ? 
                           fwd.getName().substring(0, 10) : fwd.getName());
        } else {
            fwdJersey.setText("-");
            fwdName.setText("FWD");
        }
    }

    /**
     * Enable or disable form controls
     */
    private void setFormEnabled(boolean enabled) {
        gamePlanNameField.setDisable(!enabled);
        goalkeeperCombo.setDisable(!enabled);
        defender1Combo.setDisable(!enabled);
        defender2Combo.setDisable(!enabled);
        midfielder1Combo.setDisable(!enabled);
        midfielder2Combo.setDisable(!enabled);
        forwardCombo.setDisable(!enabled);
        saveButton.setDisable(!enabled);
    }

    /**
     * Show error message
     */
    private void showError(String message) {
        statusLabel.setText("✗ " + message);
        statusLabel.setStyle("-fx-text-fill: red;");
    }

    /**
     * Go back to manager view
     */
    @FXML
    public void goBack(ActionEvent event) {
        SceneSwitcher.switchTo(event, "manager-view.fxml");
    }
}
