package com.example.coachsapp.dialog;

import com.example.coachsapp.model.Club;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AddClubDialog {

    private Club result = null;

    public Club showDialog(Stage parentStage) {
        Stage dialog = new Stage();
        dialog.setTitle("Add Club");
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(parentStage);
        dialog.setWidth(500);
        dialog.setHeight(240);

        Label clubNameLabel = new Label("Club Name:");
        TextField clubNameField = new TextField();
        clubNameField.setPromptText("Enter club name (e.g., Manchester United)");

        Button saveButton = new Button("Save");
        saveButton.getStyleClass().addAll("btn", "btn-primary");
        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().addAll("btn", "btn-secondary");

        saveButton.setOnAction(event -> {
            String clubName = clubNameField.getText().trim();

            if (clubName.isEmpty()) {
                showError("Please enter a club name");
                return;
            }

            result = new Club(clubName);
            dialog.close();
        });

        cancelButton.setOnAction(event -> dialog.close());

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(15));
        vbox.getChildren().addAll(
            clubNameLabel,
            clubNameField,
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

