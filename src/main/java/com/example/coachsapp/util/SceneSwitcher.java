package com.example.coachsapp.util;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneSwitcher {

    // Default application dimensions
    private static final double DEFAULT_SCENE_WIDTH = 800;
    private static final double DEFAULT_SCENE_HEIGHT = 600;

    public static void switchTo(ActionEvent event, String fxmlName) {
        try {
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            
            // Preserve current window dimensions
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();
            
            FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource("/com/example/coachsapp/" + fxmlName));
            Scene scene = new Scene(loader.load(), currentWidth, currentHeight);

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void switchTo(ActionEvent event, Scene currentScene, String fxmlName) {
        try {
            Stage stage = (Stage) currentScene.getWindow();
            
            // Preserve current window dimensions
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();
            
            FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource("/com/example/coachsapp/" + fxmlName));
            Scene scene = new Scene(loader.load(), currentWidth, currentHeight);

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

