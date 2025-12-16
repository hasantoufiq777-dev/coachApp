package com.example.coachsapp.ui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.layout.Region;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Animate a full-bleed gradient background by interpolating stops.
 * This provides a subtle dynamic color shift behind the UI.
 */
public class BackgroundAnimator extends Region {

    private final Timeline timeline;

    public BackgroundAnimator(double width, double height) {
        setPrefSize(width, height);
        setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        Stop[] stopsA = new Stop[] { new Stop(0, Color.web("#08103a")), new Stop(0.5, Color.web("#2a1653")), new Stop(1, Color.web("#0b3a4b")) };
        Stop[] stopsB = new Stop[] { new Stop(0, Color.web("#0b2548")), new Stop(0.5, Color.web("#3b1f66")), new Stop(1, Color.web("#053b3a")) };

        LinearGradient gA = new LinearGradient(0,0,1,1,true,CycleMethod.NO_CYCLE, stopsA);
        LinearGradient gB = new LinearGradient(1,0,0,1,true,CycleMethod.NO_CYCLE, stopsB);

        setBackground(new javafx.scene.layout.Background(new javafx.scene.layout.BackgroundFill(gA, null, null)));

        timeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(opacityProperty(), 1.0)),
            new KeyFrame(Duration.seconds(0), e -> setBackground(new javafx.scene.layout.Background(new javafx.scene.layout.BackgroundFill(gA, null, null)))),
            new KeyFrame(Duration.seconds(8), e -> setBackground(new javafx.scene.layout.Background(new javafx.scene.layout.BackgroundFill(gB, null, null)))),
            new KeyFrame(Duration.seconds(16), e -> setBackground(new javafx.scene.layout.Background(new javafx.scene.layout.BackgroundFill(gA, null, null))))
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.setAutoReverse(true);
    }

    public void start() {
        timeline.play();
    }

    public void stop() {
        timeline.stop();
    }
}
