package com.example.coachsapp.dialog;

import com.example.coachsapp.model.Manager;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AddManagerDialog {

    private Manager result = null;

    public Manager showDialog(Stage parentStage) {
        Stage dialog = new Stage();
        dialog.setTitle("Add Manager");
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(parentStage);
        dialog.setWidth(300);
        dialog.setHeight(200);

        Label nameLabel = new Label("Manager Name:");
        TextField nameField = new TextField();
        nameField.setPromptText("Enter manager name");

        Label clubLabel = new Label("Club Name:");
        TextField clubField = new TextField();
        clubField.setPromptText("Enter club name");

        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");

        saveButton.setOnAction(event -> {
            String name = nameField.getText().trim();
            String club = clubField.getText().trim();

            if (name.isEmpty() || club.isEmpty()) {
                showError("Please fill in all fields");
                return;
            }

            result = new Manager(name, club);
            dialog.close();
        });

        cancelButton.setOnAction(event -> dialog.close());

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(15));
        vbox.getChildren().addAll(
            nameLabel, nameField,
            clubLabel, clubField,
            new HBox(10, saveButton, cancelButton)
        );

        Scene scene = new Scene(vbox);
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

