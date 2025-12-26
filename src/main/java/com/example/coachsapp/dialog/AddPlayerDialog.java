package com.example.coachsapp.dialog;

import com.example.coachsapp.model.Club;
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
        dialog.setWidth(600);
        dialog.setHeight(600);

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
        ComboBox<Club> clubCombo = new ComboBox<>();
        if (!AppState.clubs.isEmpty()) {
            clubCombo.setItems(FXCollections.observableArrayList(AppState.clubs));
        } else {
            var clubsFromManagers = AppState.managers.stream().map(m -> m.getClub()).distinct().toList();
            clubCombo.setItems(FXCollections.observableArrayList(clubsFromManagers));
        }
        clubCombo.setPromptText("Select club");

        Label injuredLabel = new Label("Injured:");
        CheckBox injuredCheckBox = new CheckBox("Yes, player is injured");
        injuredCheckBox.setSelected(false);
        injuredCheckBox.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 13px;");

        Button saveButton = new Button("Save");
        saveButton.getStyleClass().addAll("btn", "btn-primary");
        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().addAll("btn", "btn-secondary");

        saveButton.setOnAction(event -> {
            String name = nameField.getText().trim();
            String ageText = ageField.getText().trim();
            String jerseyText = jerseyField.getText().trim();
            Position position = positionCombo.getValue();
            Club selectedClub = clubCombo.getValue();
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

                if (jersey <= 0 || jersey > 120) {
                    showError("Jersey number must be between 1 and 120");
                    return;
                }

                result = new Player(name, age, jersey, position, injured);
                result.setClubId(selectedClub.getId());

                System.out.println("✓ Player Created (dialog): " + name + " @ " + selectedClub.getClubName());
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
        vbox.getStyleClass().add("app-root");

        Scene scene = new Scene(vbox);
        scene.getStylesheets().add(getClass().getResource("/com/example/coachsapp/styles.css").toExternalForm());
        dialog.setScene(scene);
        dialog.showAndWait();

        return result;
    }


    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Input Error");
        alert.setHeaderText("Invalid Input");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
