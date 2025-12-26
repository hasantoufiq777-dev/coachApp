package com.example.coachsapp.dialog;

import com.example.coachsapp.model.Club;
import com.example.coachsapp.model.Player;
import com.example.coachsapp.model.Position;
import com.example.coachsapp.model.Role;
import com.example.coachsapp.model.User;
import com.example.coachsapp.util.AppState;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.collections.FXCollections;

public class EditPlayerDialog {

    private Player result = null;
    private Player originalPlayer;

    public EditPlayerDialog(Player player) {
        this.originalPlayer = player;
    }

    public Player showDialog(Stage parentStage) {
        if (originalPlayer == null) {
            return null;
        }

        User currentUser = AppState.currentUser;
        boolean isManager = currentUser != null && currentUser.getRole() == Role.CLUB_MANAGER;

        Stage dialog = new Stage();
        dialog.setTitle("Edit Player - " + originalPlayer.getName());
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(parentStage);
        dialog.setWidth(600);
        dialog.setHeight(550);

        Label nameLabel = new Label("Player Name:");
        TextField nameField = new TextField(originalPlayer.getName());
        nameField.setPromptText("Enter player name");

        Label ageLabel = new Label("Age (Cannot be changed):");
        Label ageDisplayLabel = new Label(String.valueOf(originalPlayer.getAge()));
        ageDisplayLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #888;");

        Label jerseyLabel = new Label("Jersey Number:");
        TextField jerseyField = new TextField(String.valueOf(originalPlayer.getJersey()));
        jerseyField.setPromptText("Enter jersey number");

        Label positionLabel = new Label("Position:");
        ComboBox<Position> positionCombo = new ComboBox<>();
        positionCombo.setItems(FXCollections.observableArrayList(Position.values()));
        positionCombo.setValue(originalPlayer.getPosition());
        positionCombo.setPromptText("Select position");

        Label clubLabel = new Label("Club:");
        Label clubDisplayLabel = null;
        ComboBox<Club> clubCombo = new ComboBox<>();
        
        if (isManager) {
            String clubName = "Unknown Club";
            if (originalPlayer.getClubView() != null && !originalPlayer.getClubView().isEmpty()) {
                clubName = originalPlayer.getClubView();
            } else if (originalPlayer.getClubId() != null) {
                clubName = AppState.clubs.stream()
                    .filter(c -> c.getId() != null && c.getId().equals(originalPlayer.getClubId()))
                    .map(Club::getClubName)
                    .findFirst()
                    .orElse("Unknown Club");
            }
            clubDisplayLabel = new Label(clubName);
            clubDisplayLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #888;");
        } else {
            if (!AppState.clubs.isEmpty()) {
                clubCombo.setItems(FXCollections.observableArrayList(AppState.clubs));
                if (originalPlayer.getClubId() != null) {
                    AppState.clubs.stream()
                        .filter(c -> c.getId() != null && c.getId().equals(originalPlayer.getClubId()))
                        .findFirst()
                        .ifPresent(clubCombo::setValue);
                }
            } else {
                var clubsFromManagers = AppState.managers.stream().map(m -> m.getClub()).distinct().toList();
                clubCombo.setItems(FXCollections.observableArrayList(clubsFromManagers));
            }
            clubCombo.setPromptText("Select club");
        }

        Label injuredLabel = new Label("Injured:");
        CheckBox injuredCheckBox = new CheckBox("Yes, player is injured");
        injuredCheckBox.setSelected(originalPlayer.isInjured());
        injuredCheckBox.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 13px;");

        Button saveButton = new Button("Save Changes");
        saveButton.getStyleClass().addAll("btn", "btn-primary");
        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().addAll("btn", "btn-secondary");

        final Label finalClubDisplayLabel = clubDisplayLabel;
        saveButton.setOnAction(event -> {
            String name = nameField.getText().trim();
            String jerseyText = jerseyField.getText().trim();
            Position position = positionCombo.getValue();
            Club selectedClub = isManager ? null : clubCombo.getValue();
            boolean injured = injuredCheckBox.isSelected();

            if (name.isEmpty() || jerseyText.isEmpty() || position == null) {
                showError("Please fill in all fields");
                return;
            }

            if (!isManager && selectedClub == null) {
                showError("Please select a club");
                return;
            }

            try {
                int jersey = Integer.parseInt(jerseyText);

                if (jersey <= 0 || jersey > 99) {
                    showError("Jersey number must be between 1 and 99");
                    return;
                }

                originalPlayer.setName(name);
                originalPlayer.setJersey(jersey);
                originalPlayer.setPosition(position);
                if (!isManager) {
                    originalPlayer.setClubId(selectedClub.getId());
                }
                originalPlayer.setInjured(injured);

                result = originalPlayer;
                String clubInfo = isManager ? originalPlayer.getClubView() : selectedClub.getClubName();
                System.out.println("✓ Player Updated (dialog): " + name + " @ " + clubInfo);
                dialog.close();
            } catch (NumberFormatException e) {
                showError("Jersey must be a valid number");
            }
        });

        cancelButton.setOnAction(event -> dialog.close());

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(15));
        
        if (isManager) {
            vbox.getChildren().addAll(
                nameLabel, nameField,
                ageLabel, ageDisplayLabel,
                jerseyLabel, jerseyField,
                positionLabel, positionCombo,
                clubLabel, clubDisplayLabel,
                injuredLabel, injuredCheckBox,
                new HBox(10, saveButton, cancelButton)
            );
        } else {
            vbox.getChildren().addAll(
                nameLabel, nameField,
                ageLabel, ageDisplayLabel,
                jerseyLabel, jerseyField,
                positionLabel, positionCombo,
                clubLabel, clubCombo,
                injuredLabel, injuredCheckBox,
                new HBox(10, saveButton, cancelButton)
            );
        }
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
