package com.example.coachsapp;

import com.example.coachsapp.db.DatabaseService;
import com.example.coachsapp.model.Club;
import com.example.coachsapp.model.Manager;
import com.example.coachsapp.model.Player;
import com.example.coachsapp.util.AppState;
import javafx.application.Application;
import com.example.coachsapp.ui.ParticlePane;
import com.example.coachsapp.ui.BackgroundAnimator;
import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;

public class HelloApplication extends Application {
  @Override
  public void start(Stage stage) throws IOException {
    try {
      DatabaseService db = DatabaseService.getInstance();


      List<Manager> managers = db.loadAllManagers();
      AppState.managers.clear();
      AppState.managers.addAll(managers);

      AppState.clubs.clear();
      for (Manager m : managers) {
        Club c = m.getClub();
        if (c != null && !AppState.clubs.contains(c)) {
          AppState.clubs.add(c);
        }
      }

      List<Player> players = db.loadAllPlayers();
      AppState.players.clear();
      AppState.players.addAll(players);

      for (Player p : players) {
        Integer cid = p.getClubId();
        if (cid != null) {
          for (Club c : AppState.clubs) {
            if (cid.equals(c.getId())) {
              c.addPlayer(p);
              break;
            }
          }
        }
      }

      System.out.println("✓ AppState populated: managers=" + AppState.managers.size() + ", clubs=" + AppState.clubs.size() + ", players=" + AppState.players.size());
    } catch (Exception e) {
      System.err.println("✗ Failed to initialize database on startup: " + e.getMessage());
      e.printStackTrace();
    }

    FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("main-view.fxml"));
    Node root = fxmlLoader.load();

    Scene scene = new Scene((javafx.scene.Parent) root, 1000, 700);

    ParticlePane particlePane = new ParticlePane();
    particlePane.setPrefSize(1000, 700);

    BackgroundAnimator bg = new BackgroundAnimator(1000, 700);

    javafx.scene.layout.StackPane stack = new javafx.scene.layout.StackPane();
    stack.getChildren().addAll(bg, particlePane, root);

    Scene stackedScene = new Scene(stack, 1000, 700);

    stackedScene.setFill(Color.web("#08103a"));
    stage.setTitle("Coaches App");
    stage.setScene(stackedScene);
    stage.show();


    bg.start();
    particlePane.start();


    FadeTransition ft = new FadeTransition(Duration.millis(450), root);
    root.setOpacity(0);
    ft.setFromValue(0);
    ft.setToValue(1);
    ft.play();
  }
}
