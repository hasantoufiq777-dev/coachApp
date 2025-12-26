module com.example.coachsapp {
    requires java.sql;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    opens com.example.coachsapp to javafx.fxml;
    opens com.example.coachsapp.controller to javafx.fxml;
    opens com.example.coachsapp.model to javafx.base;
    exports com.example.coachsapp;
    exports com.example.coachsapp.controller;
    exports com.example.coachsapp.model;
}