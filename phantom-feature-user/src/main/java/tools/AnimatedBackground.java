package tools;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Background;
import javafx.scene.paint.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AnimatedBackground {

    private static final int PARTICLE_COUNT = 50;
    private static final Random random = new Random();

    public static void addAnimatedBackground(Parent rootNode) {
        if (rootNode.getScene() != null) {
            inject(rootNode);
        } else {
            rootNode.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    Platform.runLater(() -> inject(rootNode));
                }
            });
        }
    }

    private static void inject(Parent rootNode) {
        Scene scene = rootNode.getScene();
        if (scene == null) return;

        Parent originalRoot = scene.getRoot();
        if (originalRoot == null) return;

        // Check if already injected
        if (originalRoot instanceof StackPane && "bg-wrapper".equals(originalRoot.getId())) {
            return;
        }

        StackPane wrapper = new StackPane();
        wrapper.setId("bg-wrapper");
        wrapper.setStyle("-fx-background-color: #050508;"); // Deep space black

        Canvas canvas = new Canvas();
        canvas.widthProperty().bind(wrapper.widthProperty());
        canvas.heightProperty().bind(wrapper.heightProperty());
        canvas.setMouseTransparent(true);

        scene.setRoot(wrapper);
        wrapper.getChildren().addAll(canvas, originalRoot);

        // FORCE TRANSPARENCY on the original UI root and its immediate container
        originalRoot.setStyle(originalRoot.getStyle() + "; -fx-background-color: transparent; -fx-background: transparent;");
        if (originalRoot instanceof Pane) {
            ((Pane) originalRoot).setBackground(Background.EMPTY);
        }

        startAnimation(canvas);
    }

    private static void startAnimation(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        List<Particle> particles = new ArrayList<>();
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            particles.add(new Particle());
        }

        new AnimationTimer() {
            double angle = 0;

            @Override
            public void handle(long now) {
                double w = canvas.getWidth();
                double h = canvas.getHeight();
                if (w <= 0 || h <= 0) return;

                // 1. Draw Moving Gradient Mesh
                angle += 0.005;
                drawGradientBackground(gc, w, h, angle);

                // 2. Draw Particles
                for (Particle p : particles) {
                    p.update(w, h);
                    p.draw(gc);
                }
            }
        }.start();
    }

    private static void drawGradientBackground(GraphicsContext gc, double w, double h, double angle) {
        // Create a multi-stop radial gradient that moves
        double cx = w / 2 + Math.cos(angle) * (w / 4);
        double cy = h / 2 + Math.sin(angle * 0.8) * (h / 4);

        RadialGradient bg = new RadialGradient(
            0, 0, cx, cy, Math.max(w, h), false, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#1a0a0a")), // Subtle Dark Red center
            new Stop(0.5, Color.web("#0a0a0f")), // Subtle Dark Blue/Purple
            new Stop(1, Color.web("#050508")) // Deep Black edges
        );

        gc.setFill(bg);
        gc.fillRect(0, 0, w, h);
        
        // Add a secondary moving highlight
        double hx = w / 2 + Math.sin(angle * 1.2) * (w / 3);
        double hy = h / 2 + Math.cos(angle * 0.5) * (h / 3);
        
        RadialGradient light = new RadialGradient(
            0, 0, hx, hy, w / 2, false, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(255, 45, 45, 0.05)),
            new Stop(1, Color.TRANSPARENT)
        );
        gc.setFill(light);
        gc.fillRect(0, 0, w, h);
    }

    private static class Particle {
        double x, y, r, sx, sy, op;
        Color col;

        Particle() {
            init(true);
        }

        void init(boolean randomY) {
            r = random.nextDouble() * 3 + 1;
            sx = (random.nextDouble() - 0.5) * 0.3;
            sy = -random.nextDouble() * 0.5 - 0.2;
            op = random.nextDouble() * 0.5 + 0.2;
            col = random.nextBoolean() ? Color.web("#ff2d2d") : Color.WHITE;
            x = random.nextDouble() * 1920;
            y = randomY ? random.nextDouble() * 1080 : 1080 + r;
        }

        void update(double w, double h) {
            x += sx;
            y += sy;
            if (y < -r || x < -r || x > w + r) {
                init(false);
                x = random.nextDouble() * w;
                y = h + r;
            }
        }

        void draw(GraphicsContext gc) {
            gc.setGlobalAlpha(op);
            gc.setFill(col);
            gc.fillOval(x, y, r, r);
            
            // Glow
            gc.setFill(new RadialGradient(0, 0, x, y, r * 4, false, CycleMethod.NO_CYCLE,
                new Stop(0, col.deriveColor(0, 1, 1, 0.3)),
                new Stop(1, Color.TRANSPARENT)));
            gc.fillOval(x - r * 4, y - r * 4, r * 8, r * 8);
            gc.setGlobalAlpha(1.0);
        }
    }
}
