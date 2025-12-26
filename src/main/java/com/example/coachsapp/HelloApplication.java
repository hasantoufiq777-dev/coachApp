package com.example.coachsapp;

import com.example.coachsapp.db.DatabaseService;
import com.example.coachsapp.db.DatabaseMigration;
import com.example.coachsapp.model.Club;
import com.example.coachsapp.model.Manager;
import com.example.coachsapp.model.Player;
import com.example.coachsapp.util.AppState;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class HelloApplication extends Application {
  @Override
  public void start(Stage stage) throws IOException {
    try {
      // Run database migration first
      DatabaseMigration.migrateToVersion2();
      
      DatabaseService db = DatabaseService.getInstance();


      db.getUserRepository().createTable();
      db.getTransferRequestRepository().createTable();
      db.getRegistrationRequestRepository().createTable();

      // Load all clubs directly from database
      List<Club> allClubs = db.getClubRepository().findAll();
      AppState.clubs.clear();
      AppState.clubs.addAll(allClubs);
      System.out.println("=== Loaded " + allClubs.size() + " clubs from database ===");

      // Load all managers
      List<Manager> managers = db.loadAllManagers();
      AppState.managers.clear();
      AppState.managers.addAll(managers);

      List<Player> players = db.loadAllPlayers();
      System.out.println("=== Loaded " + players.size() + " players from database ===");
      for (Player p : players) {
        System.out.println("  Player: " + p.getName() + " (ID: " + p.getId() + ", ClubID: " + p.getClubId() + ")");
      }
      AppState.players.clear();
      AppState.players.addAll(players);

      System.out.println("=== Associating players with clubs ===");
      System.out.println("Available clubs: " + AppState.clubs.size());
      for (Club c : AppState.clubs) {
        System.out.println("  Club: " + c.getClubName() + " (ID: " + c.getId() + ")");
      }
      
      for (Player p : players) {
        Integer cid = p.getClubId();
        if (cid != null) {
          boolean found = false;
          for (Club c : AppState.clubs) {
            if (cid.equals(c.getId())) {
              c.addPlayer(p);
              System.out.println("  ✓ Added player " + p.getName() + " to club " + c.getClubName());
              found = true;
              break;
            }
          }
          if (!found) {
            System.out.println("  ✗ No club found for player " + p.getName() + " (club_id: " + cid + ")");
          }
        } else {
          System.out.println("  ⚠ Player " + p.getName() + " has no club_id");
        }
      }

      com.example.coachsapp.db.UserSeeder.seedDefaultUsers(db);

      System.out.println("✓ AppState populated: managers=" + AppState.managers.size() + ", clubs=" + AppState.clubs.size() + ", players=" + AppState.players.size());
    } catch (Exception e) {
      System.err.println("✗ Failed to initialize database on startup: " + e.getMessage());
      e.printStackTrace();
    }

    FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));
    Scene scene = new Scene(fxmlLoader.load(), 800, 600);
    stage.setTitle("Coaches App");
    stage.setScene(scene);
    stage.show();
  }
}
