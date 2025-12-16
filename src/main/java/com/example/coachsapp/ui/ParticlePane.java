package com.example.coachsapp.ui;

import javafx.animation.AnimationTimer;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Lightweight particle background pane.
 * Adds subtle floating particles for a modern luxury feel.
 */
public class ParticlePane extends Pane {

    private final List<Particle> particles = new ArrayList<>();
    private final Random random = new Random();
    private final AnimationTimer timer;

    public ParticlePane() {
        setPickOnBounds(false);
        setMouseTransparent(true);
        setStyle("-fx-background-color: transparent;");

        timer = new AnimationTimer() {
            private long last = 0;

            @Override
            public void handle(long now) {
                if (last == 0) {
                    last = now;
                    return;
                }
                double dt = (now - last) / 1_000_000_000.0;
                last = now;
                update(dt);
            }
        };
    }

    public void start() {
        timer.start();
    }

    public void stop() {
        timer.stop();
    }

    private void update(double dt) {
        // occasionally emit
        if (random.nextDouble() < 0.06) {
            emitParticle();
        }

        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            p.update(dt);
            if (p.isDead()) {
                getChildren().remove(p.node);
                it.remove();
            }
        }
    }

    private void emitParticle() {
        double w = Math.max(200, getWidth());
        double h = Math.max(200, getHeight());
        double x = random.nextDouble() * w;
        double y = h + 10;

        double size = 6 + random.nextDouble() * 18;
        Circle c = new Circle(size / 2.0);
        c.setOpacity(0.0);
        Color col = Color.rgb(139, 92, 246, 0.12 + random.nextDouble() * 0.18);
        c.setFill(col);
        c.setTranslateX(x);
        c.setTranslateY(y);

        getChildren().add(c);

        Particle p = new Particle(c, -10 - random.nextDouble() * 30, -20 - random.nextDouble() * 40, 2.0 + random.nextDouble() * 6.0);
        particles.add(p);
    }

    private static class Particle {
        final Circle node;
        double vx, vy;
        double life;

        Particle(Circle node, double vx, double vy, double life) {
            this.node = node;
            this.vx = vx;
            this.vy = vy;
            this.life = life;
            node.setOpacity(0.0);
        }

        void update(double dt) {
            life -= dt;
            node.setTranslateX(node.getTranslateX() + vx * dt);
            node.setTranslateY(node.getTranslateY() + vy * dt);
            double alpha = Math.max(0, Math.min(1, (1.0 - (life / 6.0))));
            node.setOpacity(alpha * 0.9);
            node.setScaleX(0.6 + (1 - life / 6.0) * 0.6);
            node.setScaleY(node.getScaleX());
        }

        boolean isDead() {
            return life <= 0;
        }
    }
}
