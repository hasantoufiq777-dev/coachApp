package com.example.coachsapp.controller;

import com.example.coachsapp.model.Position;
import com.example.coachsapp.model.Player;
import com.example.coachsapp.util.AppState;
import com.example.coachsapp.util.SceneSwitcher;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;

public class PlayerController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField ageField;

    @FXML
    private TextField jerseyField;

    @FXML
    private ComboBox<Position> positionCombo;

    @FXML
    private ComboBox<String> clubCombo;

    @FXML
    private CheckBox injuredCheckBox;

    @FXML
    public void initialize() {
        positionCombo.setItems(FXCollections.observableArrayList(Position.values()));

        // Populate club names from managers
        clubCombo.setItems(FXCollections.observableArrayList(
            AppState.managers.stream().map(m -> m.getClub().getClubName()).distinct().collect(java.util.stream.Collectors.toList())
        ));
    }

    @FXML
    public void savePlayer() {
        String name = nameField.getText();
        String ageText = ageField.getText();
        String jerseyText = jerseyField.getText();
        Position position = positionCombo.getValue();
        String selectedClub = clubCombo.getValue();
        boolean injured = injuredCheckBox != null && injuredCheckBox.isSelected();

        if (name == null || name.isEmpty() || ageText == null || ageText.isEmpty() ||
            jerseyText == null || jerseyText.isEmpty() || position == null || selectedClub == null) {
            System.out.println("Error: All fields must be filled!");
            return;
        }

        try {
            int age = Integer.parseInt(ageText);
            int jersey = Integer.parseInt(jerseyText);

            Player player = new Player(name, age, jersey, position, injured);
            Integer clubId = getClubIdByName(selectedClub);
            player.setClubId(clubId);

            AppState.players.add(player);

            System.out.println("✓ Player Created:");
            System.out.println("Name: " + name);
            System.out.println("Age: " + age);
            System.out.println("Jersey: " + jersey);
            System.out.println("Position: " + position);
            System.out.println("Club: " + selectedClub);
            System.out.println("Injured: " + injured);

            clearFields();
        } catch (NumberFormatException e) {
            System.out.println("Error: Age and Jersey must be valid numbers!");
        }
    }

    private Integer getClubIdByName(String clubName) {
        return AppState.managers.stream()
            .filter(m -> m.getClub().getClubName().equals(clubName))
            .findFirst()
            .map(m -> m.getClub().getId())
            .orElse(null);
    }

    @FXML
    public void goBack(ActionEvent event) {
        SceneSwitcher.switchTo(event, "main-view.fxml");
    }

    private void clearFields() {
        nameField.clear();
        ageField.clear();
        jerseyField.clear();
        positionCombo.setValue(null);
        clubCombo.setValue(null);
        if (injuredCheckBox != null) {
            injuredCheckBox.setSelected(false);
        }
    }
}
