package com.example.coachsapp.controller;

import com.example.coachsapp.db.DatabaseService;
import com.example.coachsapp.model.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class RegistrationController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private TextField ageField;

    @FXML
    private ComboBox<String> roleCombo;

    @FXML
    private ComboBox<Position> positionCombo;

    @FXML
    private VBox positionBox;

    @FXML
    private ComboBox<Club> clubCombo;

    @FXML
    private Label errorLabel;

    @FXML
    private Label passwordStrengthLabel;

    private DatabaseService dbService;
    private StringProperty passwordStrength = new SimpleStringProperty("Weak");

    @FXML
    public void initialize() {
        dbService = DatabaseService.getInstance();
        
        roleCombo.setItems(FXCollections.observableArrayList("Club Manager", "Player"));
        
        positionCombo.setItems(FXCollections.observableArrayList(Position.values()));
        
        roleCombo.setOnAction(e -> {
            String selected = roleCombo.getValue();
            boolean isPlayer = "Player".equals(selected);
            positionBox.setVisible(isPlayer);
            positionBox.setManaged(isPlayer);
        });
        
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            validatePasswordStrength(newValue);
        });
        
        passwordStrength.addListener((observable, oldValue, newValue) -> {
            updatePasswordStrengthDisplay(newValue);
        });

        List<Club> clubs = new java.util.ArrayList<>(com.example.coachsapp.util.AppState.clubs);
        
        if (clubs.isEmpty()) {
            clubs = dbService.getClubRepository().findAll();
        }
        
        clubCombo.setItems(FXCollections.observableArrayList(clubs));
        
        errorLabel.setText("");
    }

    private void validatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            passwordStrength.set("Weak");
            return;
        }
        
        boolean hasSpecialChar = password.matches(".*[@#$].*");
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasNumber = password.matches(".*[0-9].*");
        boolean hasMinLength = password.length() >= 4;
        
        if (hasSpecialChar && hasLetter && hasNumber && hasMinLength) {
            passwordStrength.set("Strong");
        } else {
            passwordStrength.set("Weak");
        }
    }

    private void updatePasswordStrengthDisplay(String strength) {
        if ("Strong".equals(strength)) {
            passwordStrengthLabel.setText("Password Strength: Strong ✓");
            passwordStrengthLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: green; -fx-font-weight: bold;");
        } else {
            passwordStrengthLabel.setText("Password Strength: Weak (need @#$, letters, numbers, min 4 chars)");
            passwordStrengthLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: orange; -fx-font-weight: bold;");
        }
    }

    @FXML
    public void handleRegister() {
        errorLabel.setText("");

        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String ageText = ageField.getText().trim();
        String selectedRoleStr = roleCombo.getValue();
        Position selectedPosition = positionCombo.getValue();
        Club selectedClub = clubCombo.getValue();
        
        if (username.isEmpty()) {
            errorLabel.setText("Username is required");
            return;
        }
        
        if (password.isEmpty()) {
            errorLabel.setText("Password is required");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            errorLabel.setText("Passwords do not match");
            return;
        }
        
        if (password.length() < 4) {
            errorLabel.setText("Password must be at least 4 characters");
            return;
        }
        
        boolean hasSpecialChar = password.matches(".*[@#$].*");
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasNumber = password.matches(".*[0-9].*");
        
        if (!hasSpecialChar || !hasLetter || !hasNumber) {
            errorLabel.setText("Password must contain @, # or $, letters and numbers");
            return;
        }
        
        if (ageText.isEmpty()) {
            errorLabel.setText("Age is required");
            return;
        }
        
        int age;
        try {
            age = Integer.parseInt(ageText);
            if (age < 1 || age > 100) {
                errorLabel.setText("Age must be between 1 and 100");
                return;
            }
        } catch (NumberFormatException e) {
            errorLabel.setText("Age must be a valid number");
            return;
        }
        
        if (selectedRoleStr == null) {
            errorLabel.setText("Please select a role");
            return;
        }
        
        if ("Player".equals(selectedRoleStr) && selectedPosition == null) {
            errorLabel.setText("Please select a position");
            return;
        }
        
        if (selectedClub == null) {
            errorLabel.setText("Please select a club");
            return;
        }

        User existingUser = dbService.getUserRepository().findByUsername(username);
        if (existingUser != null) {
            errorLabel.setText("Username already exists");
            return;
        }

        RegistrationRequest existingRequest = dbService.getRegistrationRequestRepository().findByUsername(username);
        if (existingRequest != null) {
            errorLabel.setText("Registration request already submitted");
            return;
        }
        
        Role requestedRole = selectedRoleStr.equals("Club Manager") ? Role.CLUB_MANAGER : Role.PLAYER;

        RegistrationRequest request = new RegistrationRequest(
            username, 
            password, 
            username, 
            requestedRole, 
            selectedClub.getId()
        );
        request.setAge(age);
        if (selectedPosition != null) {
            request.setPosition(selectedPosition.toString());
        }
        
        dbService.getRegistrationRequestRepository().save(request);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Registration Request Submitted");
        alert.setHeaderText(null);
        alert.setContentText("Your registration request has been submitted successfully!\n\n" +
                           "An admin will review your request and approve it.\n" +
                           "You will be able to login once approved.");
        alert.showAndWait();

        goBackToLogin();
    }

    @FXML
    public void goBackToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/coachsapp/login-view.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            errorLabel.setText("Error loading login screen: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
