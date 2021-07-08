package main.java.view.main;

import javafx.animation.AnimationTimer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.transform.Rotate;
import lombok.Getter;
import main.java.model.Vector2D;
import main.java.model.world.Base;
import main.java.model.world.Entity;
import main.java.model.world.rockets.AdvancedInterceptorRocket;
import main.java.model.world.rockets.FlakRocket;
import main.java.model.world.rockets.Rocket;
import main.java.model.world.rockets.SimpleInterceptorRocket;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class MapView extends Canvas {

    @Getter
    private ListProperty<Entity> entities;
    @Getter
    private DoubleProperty centerWorldX;
    @Getter
    private DoubleProperty centerWorldY;
    @Getter
    private DoubleProperty zoom;
    @Getter
    private DoubleProperty worldWidth;
    @Getter
    private DoubleProperty worldHeight;

    Image backgroundImage;

    Color attackerColor = new Color(0.59, 0.21, 0.19, 1);
    Color defenderColor = new Color(0.21, 0.19, 0.59, 1);

    public MapView(double width, double height) {
        super(width, height);
        init();
    }

    public MapView() {
        super();
        init();
    }

    private void init() {
        heightProperty().addListener((observable, oldValue, newValue) -> render());
        widthProperty().addListener((observable, oldValue, newValue) -> render());

        entities = new SimpleListProperty<>(FXCollections.observableArrayList());
        entities.addListener((ListChangeListener<? super Entity>) c -> render(true));

        centerWorldX = new SimpleDoubleProperty(0);
        centerWorldX.addListener(c -> render());

        centerWorldY = new SimpleDoubleProperty(0);
        centerWorldY.addListener(c -> render());

        zoom = new SimpleDoubleProperty(1);
        zoom.addListener(c -> render());

        worldWidth = new SimpleDoubleProperty(1);
        worldWidth.addListener(c -> render());

        worldHeight = new SimpleDoubleProperty(1);
        worldHeight.addListener(c -> render());

        backgroundImage = new Image("map_2.jpg");

        frameWatcher.start();
        render();
    }

    public long worldToCanvasLength(double worldLength) {
        return Math.round(worldLength * this.zoom.getValue());
    }

    public double canvasToWorldLength(double canvasLength) {
        return canvasLength / this.zoom.getValue();
    }

    public Vector2D worldToCanvasCoordinates(Vector2D worldCoordinates) {
        final double sectionWorldWidth = this.getWidth() / zoom.get();
        final double sectionWorldHeight = this.getHeight() / zoom.get();
        final Vector2D sectionTopLeft = new Vector2D(centerWorldX.get() - 0.5 * (sectionWorldWidth), centerWorldY.get() - 0.5 * (sectionWorldHeight));

        return new Vector2D(
                (worldCoordinates.x - sectionTopLeft.x) / sectionWorldWidth * this.getWidth(),
                (worldCoordinates.y - sectionTopLeft.y) / sectionWorldHeight * this.getHeight());
    }

    public Vector2D canvasToWorldCoordinates(Vector2D canvasCoordinates) {
        final double sectionWorldWidth = this.getWidth() / zoom.get();
        final double sectionWorldHeight = this.getHeight() / zoom.get();
        final Vector2D sectionTopLeft = new Vector2D(centerWorldX.get() - 0.5f * (sectionWorldWidth), centerWorldY.get() - 0.5f * (sectionWorldHeight));

        return new Vector2D(
                sectionTopLeft.x + sectionWorldWidth * (canvasCoordinates.x / this.getWidth()),
                sectionTopLeft.y + sectionWorldHeight * (canvasCoordinates.y / this.getHeight()));
    }

    private void drawLight(double x, double y, double radius, Color color, double intensity) {
        GraphicsContext gc = this.getGraphicsContext2D();
        gc.save();

        gc.setGlobalAlpha(intensity);
        gc.setGlobalBlendMode(BlendMode.SRC_OVER);
        gc.setFill(new RadialGradient(0, 0, 0.5, 0.5, 0.5, true, CycleMethod.REFLECT, new Stop(0.25, color), new Stop(1.0, Color.rgb(0, 0, 0, 0))));
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);

        gc.restore();

    }

    private Color getSideColor(Entity entity) {
        switch (entity.getSide()) {
            case ATTACKER -> {
                return attackerColor;
            }
            case DEFENDER -> {
                return defenderColor;
            }
            default -> {
                return Color.WHITE;
            }
        }
    }

    private void drawPointPath(Vector2D origin, Vector2D[] points) {
        GraphicsContext gc = this.getGraphicsContext2D();

        gc.save();
        gc.translate(origin.x, origin.y);
        gc.beginPath();
        for (int i = 1; i < points.length; i++) {
            gc.translate(worldToCanvasLength(points[i - 1].x), worldToCanvasLength(points[i - 1].y));
            gc.lineTo(
                    worldToCanvasLength(points[i].x - points[i - 1].x),
                    worldToCanvasLength(points[i].y - points[i - 1].y)
            );
            gc.translate(worldToCanvasLength(points[i - 1].x) * -1, worldToCanvasLength(points[i - 1].y) * -1);
        }
        gc.closePath();
        gc.restore();
    }

    private void drawFlakRocket(Vector2D pos, double angle) {
        GraphicsContext gc = this.getGraphicsContext2D();

        gc.save();
        double degrees = angle / Math.PI * -180;
        rotate(gc, degrees, pos.x, pos.y);

        Vector2D[] points = {
                new Vector2D(0, 0),
                new Vector2D(0, -2),
                new Vector2D(0.5, -3),
                new Vector2D(1, -2),
                new Vector2D(1, 0),
                new Vector2D(0, 0),
        };

        drawPointPath(pos.add(new Vector2D(worldToCanvasLength(-0.5), worldToCanvasLength(2))), points);
        gc.fill();

        gc.restore();
    }

    private void drawAdvancedInterceptorRocket(Vector2D pos, double angle) {
        GraphicsContext gc = this.getGraphicsContext2D();

        gc.save();
        gc.setLineWidth(worldToCanvasLength(0.5));
        double degrees = angle / Math.PI * -180;
        rotate(gc, degrees, pos.x, pos.y);

        double size = 5;

        double length = 3 * size;
        double width = 0.5 * size;
        double tipSize = 1 * size;
        double wingPosition = 0.2;
        double wingLength = 0.5 * size;
        double wingWidth = 0.7 * size;
        double wingShear = 0.2 * size;
        double sideLength = length - tipSize;
        double wingBottomAttachmentHeight = (sideLength - wingWidth) * wingPosition;
        double wingTopAttachmentHeight = wingBottomAttachmentHeight + wingWidth;
        double rearWidthFactor = 0.5;
        double frontWingSizeRatio = 0.5;
        double frontWingOffset = length / 10;

        double frontWingTipHeight = sideLength - frontWingOffset - (wingWidth + wingShear) * frontWingSizeRatio;
        Vector2D[] points = {
                new Vector2D(width * (0.5 - rearWidthFactor / 2), 0),
                // Left Main Wing
                new Vector2D(0, -wingBottomAttachmentHeight),
                new Vector2D(-wingLength, -(wingBottomAttachmentHeight - wingShear)),
                new Vector2D(0, -wingTopAttachmentHeight),
                // Left Front Wing
                new Vector2D(0, -(sideLength - frontWingOffset - (wingWidth * frontWingSizeRatio))),
                new Vector2D(-wingLength * frontWingSizeRatio, -frontWingTipHeight),
                new Vector2D(0, -(sideLength - frontWingOffset)),
                // Tip
                new Vector2D(0, -sideLength),
                new Vector2D(width / 2.0, -length),
                new Vector2D(width, -sideLength),
                // Right Front Wing
                new Vector2D(width, -(sideLength - frontWingOffset)),
                new Vector2D(width + wingLength * frontWingSizeRatio, -frontWingTipHeight),
                new Vector2D(width, -(sideLength - frontWingOffset - (wingWidth * frontWingSizeRatio))),
                // Right Main Wing
                new Vector2D(width, -wingTopAttachmentHeight),
                new Vector2D(width + wingLength, -(wingBottomAttachmentHeight - wingShear)),
                new Vector2D(width, -wingBottomAttachmentHeight),
                // End
                new Vector2D(width * (0.5 + rearWidthFactor / 2), 0),
                new Vector2D(width * (0.5 - rearWidthFactor / 2), 0),
        };

        drawPointPath(pos.add(new Vector2D(worldToCanvasLength(-width / 2), worldToCanvasLength(length / 2))), points);
        gc.fill();

        gc.restore();
    }

    private void drawSimpleInterceptorRocket(Vector2D pos, double angle) {
        GraphicsContext gc = this.getGraphicsContext2D();

        gc.save();
        gc.setLineWidth(worldToCanvasLength(0.5));
        double degrees = angle / Math.PI * -180;
        rotate(gc, degrees, pos.x, pos.y);

        double size = 4;

        double length = 3 * size;
        double width = 0.5 * size;
        double tipSize = 1 * size;
        double wingPosition = 0.2;
        double wingLength = 0.5 * size;
        double wingWidth = 0.7 * size;
        double wingShear = 0.2 * size;
        double sideLength = length - tipSize;
        double wingBottomAttachmentHeight = (sideLength - wingWidth) * wingPosition;
        double wingTopAttachmentHeight = wingBottomAttachmentHeight + wingWidth;
        double rearWidthFactor = 0.5;

        Vector2D[] points = {
                new Vector2D(width * (0.5 - rearWidthFactor / 2), 0),
                // Left Main Wing
                new Vector2D(0, -wingBottomAttachmentHeight),
                new Vector2D(-wingLength, -(wingBottomAttachmentHeight - wingShear)),
                new Vector2D(0, -wingTopAttachmentHeight),
                // Tip
                new Vector2D(0, -sideLength),
                new Vector2D(width / 2.0, -length),
                new Vector2D(width, -sideLength),
                // Right Main Wing
                new Vector2D(width, -wingTopAttachmentHeight),
                new Vector2D(width + wingLength, -(wingBottomAttachmentHeight - wingShear)),
                new Vector2D(width, -wingBottomAttachmentHeight),
                // End
                new Vector2D(width * (0.5 + rearWidthFactor / 2), 0),
                new Vector2D(width * (0.5 - rearWidthFactor / 2), 0),
        };

        drawPointPath(pos.add(new Vector2D(worldToCanvasLength(-width / 2), worldToCanvasLength(length / 2))), points);
        gc.fill();

        gc.restore();
    }

    private void drawRocket(Vector2D pos, double angle) {
        GraphicsContext gc = this.getGraphicsContext2D();

        gc.save();
        gc.setLineWidth(worldToCanvasLength(0.5));
        double degrees = angle / Math.PI * -180;
        rotate(gc, degrees, pos.x, pos.y);

        double size = 4;

        double length = 6 * size;
        double width = 1 * size;
        double tipSize = 1 * size;
        double wingPosition = 0.4;
        double wingLength = 1 * size;
        double wingWidth = 0.7 * size;
        double wingShear = 0.5 * size;
        double sideLength = length - tipSize;
        double wingBottomAttachmentHeight = (sideLength - wingWidth) * wingPosition;
        double wingTopAttachmentHeight = wingBottomAttachmentHeight + wingWidth;
        double rearWingSizeRation = 0.5;
        double rearWingOffset = length / 10;
        double rearWidthFactor = 0.5;

        Vector2D[] points = {
                new Vector2D(width * (0.5 - rearWidthFactor / 2), 0),
                // Left Rear Wing
                new Vector2D(0, -rearWingOffset),
                new Vector2D(-wingLength * rearWingSizeRation, wingShear * rearWingSizeRation - rearWingOffset),
                new Vector2D(-wingLength * rearWingSizeRation, -(wingWidth - wingShear) * rearWingSizeRation - rearWingOffset),
                new Vector2D(0, -(wingWidth) * rearWingSizeRation - rearWingOffset),
                // Left Main Wing
                new Vector2D(0, -wingBottomAttachmentHeight),
                new Vector2D(-wingLength, -(wingBottomAttachmentHeight - wingShear)),
                new Vector2D(-wingLength, -(wingTopAttachmentHeight - wingShear)),
                new Vector2D(0, -wingTopAttachmentHeight),
                // Tip
                new Vector2D(0, -sideLength),
                new Vector2D(width / 2.0, -length),
                new Vector2D(width, -sideLength),
                // Right Main Wing
                new Vector2D(width, -wingTopAttachmentHeight),
                new Vector2D(width + wingLength, -(wingTopAttachmentHeight - wingShear)),
                new Vector2D(width + wingLength, -(wingBottomAttachmentHeight - wingShear)),
                new Vector2D(width, -wingBottomAttachmentHeight),
                // Right Rear Wing
                new Vector2D(width, -wingWidth * rearWingSizeRation - rearWingOffset),
                new Vector2D(width + wingLength * rearWingSizeRation, -(wingWidth - wingShear) * rearWingSizeRation - rearWingOffset),
                new Vector2D(width + wingLength * rearWingSizeRation, wingShear * rearWingSizeRation - rearWingOffset),
                new Vector2D(width, -rearWingOffset),
                // End
                new Vector2D(width * (0.5 + rearWidthFactor / 2), 0),
                new Vector2D(width * (0.5 - rearWidthFactor / 2), 0),
        };

        drawPointPath(pos.add(new Vector2D(worldToCanvasLength(-width / 2), worldToCanvasLength(length / 2))), points);
        gc.fill();

        gc.restore();
    }

    private void drawEntities() {
        GraphicsContext gc = this.getGraphicsContext2D();

        ArrayList<Entity> entities = this.entities.getValue().stream().filter(entity -> entity != null).collect(Collectors.toCollection(ArrayList::new));
        double currentTime = entities.size() > 0 ? entities.get(0).getWorld().getCurrentTime() : 0;

        entities.stream().filter(entity -> entity instanceof Rocket).forEach(entity -> {
            final Vector2D pos = worldToCanvasCoordinates(entity.getPosition());
            drawLight(pos.x, pos.y, worldToCanvasLength(10), getSideColor(entity), 0.25);
        });

        entities.stream().filter(entity -> entity instanceof Rocket).forEach(entity -> {
            gc.setFill(getSideColor(entity));
            gc.setStroke(getSideColor(entity));
            final Vector2D pos = worldToCanvasCoordinates(entity.getPosition());

            Vector2D velocity = ((Rocket) entity).getVelocity();
            Vector2D up = new Vector2D(0, -1);

            if (entity.isDestroyed()) {
                double length = 2;
                double progress = Math.min(1, (currentTime - entity.getDestructionTime()) / length);

                double intensity = 0.8 * (1 - Math.pow(progress, 2));
                double size = 20 * (1 - Math.pow(progress, 2));

                drawLight(pos.x, pos.y, worldToCanvasLength(size), Color.PAPAYAWHIP, intensity);
            } else if (entity instanceof FlakRocket) {
                drawFlakRocket(pos, velocity.signedAngle(up));
            } else if (entity instanceof SimpleInterceptorRocket) {
                drawSimpleInterceptorRocket(pos, velocity.signedAngle(up));
            } else if (entity instanceof AdvancedInterceptorRocket) {
                drawAdvancedInterceptorRocket(pos, velocity.signedAngle(up));
            } else if (entity.getClass().equals(Rocket.class)) {
                drawRocket(pos, velocity.signedAngle(up));
            }
        });

        entities.stream().filter(entity -> entity instanceof Base).forEach(entity -> {
            gc.setStroke(getSideColor(entity));
            gc.setFill(getSideColor(entity));
            final Vector2D pos = worldToCanvasCoordinates(entity.getPosition());


            double diameter = worldToCanvasLength(10);
            drawLight(pos.x, pos.y, worldToCanvasLength(15), getSideColor(entity), 0.25);
            gc.fillOval(pos.x - diameter / 2, pos.y - diameter / 2, diameter, diameter);
            gc.setFont(Font.font("sans-serif", 15));
            gc.setFill(Color.BLACK);
            gc.fillText(((Base) entity).getName(), pos.x + diameter, pos.y);

            double test = worldToCanvasLength(500);
            gc.setLineWidth(2);
            gc.strokeOval(pos.x - test / 2, pos.y - test / 2, test, test);
        });
    }

    private void rotate(GraphicsContext gc, double angle, double px, double py) {
        Rotate r = new Rotate(angle, px, py);
        gc.setTransform(r.getMxx(), r.getMyx(), r.getMxy(), r.getMyy(), r.getTx(), r.getTy());
    }

    private void drawBackground() {
        GraphicsContext gc = this.getGraphicsContext2D();

        final Vector2D worldTopLeft = worldToCanvasCoordinates(Vector2D.ZERO());
        final Vector2D worldBottomRight = worldToCanvasCoordinates(new Vector2D(worldWidth.get(), worldHeight.get()));

        gc.setStroke(Color.gray(0.3));

        final Vector2D canvasTopLeft = canvasToWorldCoordinates(Vector2D.ZERO());
        final Vector2D canvasBottomRight = canvasToWorldCoordinates(new Vector2D(getWidth(), getHeight()));

        final double zoom = this.zoom.get();
        final double gridSpacing = 40 / Math.pow(2, Math.floor(Math.log(zoom)));
        final int majorGrid = 4;

        final double startX = canvasTopLeft.x - (canvasTopLeft.x % (gridSpacing * majorGrid) + gridSpacing * majorGrid);
        for (int i = 0; i < (canvasBottomRight.x - startX) / gridSpacing; i++) {
            Vector2D canvas = worldToCanvasCoordinates(new Vector2D(startX + gridSpacing * i, 0));
            if (i % majorGrid == 0) {
                gc.setLineWidth(3);
            } else {
                gc.setLineWidth(1);
            }
            gc.strokeLine(canvas.x, 0, canvas.x, this.getHeight());
        }

        final double startY = canvasTopLeft.y - (canvasTopLeft.y % (gridSpacing * majorGrid) + gridSpacing * majorGrid);
        for (int i = 0; i < (canvasBottomRight.y - startY) / gridSpacing; i++) {
            Vector2D canvas = worldToCanvasCoordinates(new Vector2D(0, startY + gridSpacing * i));
            if (i % majorGrid == 0) {
                gc.setLineWidth(3);
            } else {
                gc.setLineWidth(1);
            }
            gc.strokeLine(0, canvas.y, this.getWidth(), canvas.y);

        }


        final float borderWidth = 4;
        gc.setFill(Color.PAPAYAWHIP);
        gc.fillRect(worldTopLeft.x - borderWidth, worldTopLeft.y - borderWidth, worldBottomRight.x - worldTopLeft.x + borderWidth * 2, worldBottomRight.y - worldTopLeft.y + borderWidth * 2);
        gc.drawImage(backgroundImage, worldTopLeft.x, worldTopLeft.y, worldBottomRight.x - worldTopLeft.x, worldBottomRight.y - worldTopLeft.y);
    }

    private void render() {
        render(false);
    }


    double smoothing = 0.7;
    double smoothedFrameTime = 0;
    long lastFrame = 0;
    long lastFrameTime = 0;
    AnimationTimer frameWatcher = new AnimationTimer() {
        @Override
        public void handle(long now) {
            lastFrameTime = now - lastFrame;
            smoothedFrameTime = (smoothedFrameTime * smoothing) + ((lastFrameTime) * (1.0 - smoothing));
            lastFrame = now;
        }
    };

    private void render(boolean isEntityUpdate) {
        GraphicsContext gc = this.getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());

        drawBackground();
        drawEntities();
        drawStats();
    }

    private void drawStats() {
        GraphicsContext gc = this.getGraphicsContext2D();

        StringBuilder sb = new StringBuilder();

        sb.append(String.format("E: %d; \n", entities.size()));
        sb.append(String.format("T: %.2f; \n", entities.size() > 0 ? entities.get(0).getWorld().getCurrentTime() : 0));
        sb.append(String.format("F: %.2f; \n", 1000000000 / smoothedFrameTime));
        sb.append(String.format("f: %.2fms; \n", lastFrameTime / 1000000d));
        sb.append(String.format("S: %.2f; \n", entities.size() > 0 ? entities.get(0).getWorld().getSimulationSpeed() : 0));
        sb.append(String.format("U: %d; \n", entities.size() > 0 ? entities.get(0).getWorld().getUpdateInterval() : 0));

        gc.setTextBaseline(VPos.TOP);
        gc.setFill(Color.PAPAYAWHIP);
        gc.fillText(sb.toString(), 5, 5);

    }

    public void setEntityList(ObservableList<Entity> entities) {
        this.entities.set(entities);
    }

    public ObservableList<Entity> getEntityList() {
        return entities.get();
    }
}
