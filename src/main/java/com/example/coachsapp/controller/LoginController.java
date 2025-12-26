package com.example.coachsapp.controller;

import com.example.coachsapp.db.DatabaseService;
import com.example.coachsapp.model.User;
import com.example.coachsapp.util.AppState;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    public void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter username and password");
            return;
        }

        DatabaseService dbService = DatabaseService.getInstance();
        User user = dbService.getUserRepository().authenticate(username, password);

        if (user != null) {

            AppState.currentUser = user;
            

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/coachsapp/main-view.fxml"));
                Scene scene = new Scene(loader.load());
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Coaches App - " + user.getRole().getDisplayName());
            } catch (Exception e) {
                errorLabel.setText("Error loading main view: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            errorLabel.setText("Invalid username or password");
            passwordField.clear();
        }
    }

    @FXML
    public void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/coachsapp/registration-view.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Coaches App - Registration");
        } catch (Exception e) {
            errorLabel.setText("Error loading registration screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        errorLabel.setText("");
    }
}
