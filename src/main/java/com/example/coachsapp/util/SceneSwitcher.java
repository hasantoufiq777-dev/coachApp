package com.example.coachsapp.util;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneSwitcher {

    public static void switchTo(ActionEvent event, String fxmlName) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource("/com/example/coachsapp/" + fxmlName));
            Scene scene = new Scene(loader.load(), 1000, 700);

            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void switchTo(ActionEvent event, Scene currentScene, String fxmlName) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource("/com/example/coachsapp/" + fxmlName));
            Scene scene = new Scene(loader.load(), 1000, 700);

            Stage stage = (Stage) currentScene.getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

