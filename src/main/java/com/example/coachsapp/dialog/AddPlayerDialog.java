package com.example.coachsapp.dialog;

import com.example.coachsapp.model.Player;
import com.example.coachsapp.model.Position;
import com.example.coachsapp.util.AppState;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.collections.FXCollections;

public class AddPlayerDialog {

    private Player result = null;

    public Player showDialog(Stage parentStage) {
        Stage dialog = new Stage();
        dialog.setTitle("Add Player");
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(parentStage);
        dialog.setWidth(400);
        dialog.setHeight(400);

        Label nameLabel = new Label("Player Name:");
        TextField nameField = new TextField();
        nameField.setPromptText("Enter player name");

        Label ageLabel = new Label("Age:");
        TextField ageField = new TextField();
        ageField.setPromptText("Enter age");

        Label jerseyLabel = new Label("Jersey Number:");
        TextField jerseyField = new TextField();
        jerseyField.setPromptText("Enter jersey number");

        Label positionLabel = new Label("Position:");
        ComboBox<Position> positionCombo = new ComboBox<>();
        positionCombo.setItems(FXCollections.observableArrayList(Position.values()));
        positionCombo.setPromptText("Select position");

        Label clubLabel = new Label("Club:");
        ComboBox<String> clubCombo = new ComboBox<>();
        // Populate club names from managers
        clubCombo.setItems(FXCollections.observableArrayList(
            AppState.managers.stream().map(m -> m.getClub().getClubName()).distinct().collect(java.util.stream.Collectors.toList())
        ));
        clubCombo.setPromptText("Select club");

        Label injuredLabel = new Label("Injured:");
        CheckBox injuredCheckBox = new CheckBox("Yes, player is injured");
        injuredCheckBox.setSelected(false);

        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");

        saveButton.setOnAction(event -> {
            String name = nameField.getText().trim();
            String ageText = ageField.getText().trim();
            String jerseyText = jerseyField.getText().trim();
            Position position = positionCombo.getValue();
            String selectedClub = clubCombo.getValue();
            boolean injured = injuredCheckBox.isSelected();

            if (name.isEmpty() || ageText.isEmpty() || jerseyText.isEmpty() || position == null || selectedClub == null) {
                showError("Please fill in all fields");
                return;
            }

            try {
                int age = Integer.parseInt(ageText);
                int jersey = Integer.parseInt(jerseyText);

                if (age <= 0 || age > 100) {
                    showError("Age must be between 1 and 100");
                    return;
                }

                if (jersey <= 0 || jersey > 99) {
                    showError("Jersey number must be between 1 and 99");
                    return;
                }

                result = new Player(name, age, jersey, position, injured);
                result.setClubId(getClubIdByName(selectedClub));
                dialog.close();
            } catch (NumberFormatException e) {
                showError("Age and Jersey must be valid numbers");
            }
        });

        cancelButton.setOnAction(event -> dialog.close());

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(15));
        vbox.getChildren().addAll(
            nameLabel, nameField,
            ageLabel, ageField,
            jerseyLabel, jerseyField,
            positionLabel, positionCombo,
            clubLabel, clubCombo,
            injuredLabel, injuredCheckBox,
            new HBox(10, saveButton, cancelButton)
        );

        Scene scene = new Scene(vbox);
        dialog.setScene(scene);
        dialog.showAndWait();

        return result;
    }

    private Integer getClubIdByName(String clubName) {
        return AppState.managers.stream()
            .filter(m -> m.getClub().getClubName().equals(clubName))
            .findFirst()
            .map(m -> m.getClub().getId())
            .orElse(null);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Input Error");
        alert.setHeaderText("Invalid Input");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
