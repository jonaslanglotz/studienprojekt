package main.java.view.main;

import javafx.animation.AnimationTimer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.transform.Rotate;
import lombok.Getter;
import main.java.model.Vector2D;
import main.java.model.WorldModel;
import main.java.model.world.Base;
import main.java.model.world.Entity;
import main.java.model.world.rockets.AdvancedInterceptorRocket;
import main.java.model.world.rockets.FlakRocket;
import main.java.model.world.rockets.Rocket;
import main.java.model.world.rockets.SimpleInterceptorRocket;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MapView extends StackPane {

    @Getter
    private ObjectProperty<WorldModel> worldModel;
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

    Canvas backgroundCanvas;
    Canvas lightCanvas;
    Canvas foregroundCanvas;
    GraphicsContext background;
    GraphicsContext light;
    GraphicsContext foreground;

    public MapView() {
        super();
        init();
    }

    private void init() {
        worldModel = new SimpleObjectProperty<>();
        centerWorldX = new SimpleDoubleProperty(0);
        centerWorldX.addListener(observable -> backgroundMustRerender = true);
        centerWorldY = new SimpleDoubleProperty(0);
        centerWorldY.addListener(observable -> backgroundMustRerender = true);
        zoom = new SimpleDoubleProperty(1);
        zoom.addListener(observable -> backgroundMustRerender = true);
        worldWidth = new SimpleDoubleProperty(1);
        worldWidth.addListener(observable -> backgroundMustRerender = true);
        worldHeight = new SimpleDoubleProperty(1);
        worldHeight.addListener(observable -> backgroundMustRerender = true);

        widthProperty().addListener(observable -> backgroundMustRerender = true);
        heightProperty().addListener(observable -> backgroundMustRerender = true);

        backgroundCanvas = new Canvas(getWidth(), getHeight());
        lightCanvas = new Canvas(getWidth(), getHeight());
        foregroundCanvas = new Canvas(getWidth(), getHeight());

        background = backgroundCanvas.getGraphicsContext2D();
        light = lightCanvas.getGraphicsContext2D();
        foreground = foregroundCanvas.getGraphicsContext2D();

        this.getChildren().add(backgroundCanvas);
        this.getChildren().add(lightCanvas);
        this.getChildren().add(foregroundCanvas);


        backgroundCanvas.widthProperty().bind(maxWidthProperty());
        backgroundCanvas.heightProperty().bind(maxHeightProperty());
        lightCanvas.widthProperty().bind(maxWidthProperty());
        lightCanvas.heightProperty().bind(maxHeightProperty());
        foregroundCanvas.widthProperty().bind(maxWidthProperty());
        foregroundCanvas.heightProperty().bind(maxHeightProperty());

        backgroundImage = new Image("map_2.jpg");

        frameWatcher.start();
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
        light.save();

        light.setGlobalBlendMode(BlendMode.SRC_OVER);
        light.setFill(new RadialGradient(0, 0, 0.5, 0.5, 0.5, true, CycleMethod.REFLECT, new Stop(0.25, color.deriveColor(0, 1, 1, intensity)), new Stop(1.0, Color.rgb(0, 0, 0, 0))));
        light.fillOval(x - radius, y - radius, radius * 2, radius * 2);

        light.restore();

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
        foreground.save();
        foreground.translate(origin.x, origin.y);
        foreground.beginPath();
        for (int i = 1; i < points.length; i++) {
            foreground.translate(worldToCanvasLength(points[i - 1].x), worldToCanvasLength(points[i - 1].y));
            foreground.lineTo(
                    worldToCanvasLength(points[i].x - points[i - 1].x),
                    worldToCanvasLength(points[i].y - points[i - 1].y)
            );
            foreground.translate(worldToCanvasLength(points[i - 1].x) * -1, worldToCanvasLength(points[i - 1].y) * -1);
        }
        foreground.closePath();
        foreground.restore();
    }

    private void drawFlakRocket(Vector2D pos, double angle) {
        foreground.save();
        double degrees = angle / Math.PI * -180;
        rotate(foreground, degrees, pos.x, pos.y);

        Vector2D[] points = {
                new Vector2D(0, 0),
                new Vector2D(0, -2),
                new Vector2D(0.5, -3),
                new Vector2D(1, -2),
                new Vector2D(1, 0),
                new Vector2D(0, 0),
        };

        drawPointPath(pos.add(new Vector2D(worldToCanvasLength(-0.5), worldToCanvasLength(2))), points);
        foreground.fill();

        foreground.restore();
    }

    private void drawAdvancedInterceptorRocket(Vector2D pos, double angle) {
        foreground.save();
        foreground.setLineWidth(worldToCanvasLength(0.5));
        double degrees = angle / Math.PI * -180;
        rotate(foreground, degrees, pos.x, pos.y);

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
        foreground.fill();

        foreground.restore();
    }

    private void drawSimpleInterceptorRocket(Vector2D pos, double angle) {
        foreground.save();
        foreground.setLineWidth(worldToCanvasLength(0.5));
        double degrees = angle / Math.PI * -180;
        rotate(foreground, degrees, pos.x, pos.y);

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
        foreground.fill();

        foreground.restore();
    }

    private void drawRocket(Vector2D pos, double angle) {
        foreground.save();
        foreground.setLineWidth(worldToCanvasLength(0.5));
        double degrees = angle / Math.PI * -180;
        rotate(foreground, degrees, pos.x, pos.y);

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
        foreground.fill();

        foreground.restore();
    }

    private void drawEntities(List<Entity> entities) {
        double currentTime = entities.size() > 0 ? entities.get(0).getWorld().getCurrentTime() : 0;

        entities.stream().filter(entity -> entity instanceof Rocket && !entity.isDestroyed()).forEach(entity -> {
            final Vector2D pos = worldToCanvasCoordinates(entity.getPosition());
            drawLight(pos.x, pos.y, worldToCanvasLength(10), getSideColor(entity), 0.25);
        });

        entities.stream().filter(entity -> entity instanceof Rocket).forEach(entity -> {
            foreground.setFill(getSideColor(entity));
            foreground.setStroke(getSideColor(entity));
            final Vector2D pos = worldToCanvasCoordinates(entity.getPosition());

            Vector2D velocity = ((Rocket) entity).getVelocity();
            Vector2D up = new Vector2D(0, -1);

            if (entity.isDestroyed()) {
                double innerLength = 0.25;
                double outerLength = 2 * innerLength;

                double innerProgress = Math.min(1, (currentTime - entity.getDestructionTime()) / innerLength);
                double outerProgress = Math.min(1, (currentTime - entity.getDestructionTime()) / outerLength);

                double innerStrength = (Math.sqrt(innerProgress) - innerProgress) * 4;
                double outerStrength = (Math.sqrt(outerProgress) - outerProgress) * 4;

                drawLight(pos.x, pos.y, worldToCanvasLength(100 * outerStrength), Color.PAPAYAWHIP, 0.005 * outerStrength);
                drawLight(pos.x, pos.y, worldToCanvasLength(40 * outerStrength), Color.PAPAYAWHIP, 0.1 * outerStrength);
                drawLight(pos.x, pos.y, worldToCanvasLength(10 * innerStrength), Color.rgb(254, 248, 230, 1), 10 * innerStrength);
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
            foreground.setStroke(getSideColor(entity));
            foreground.setFill(getSideColor(entity));
            final Vector2D pos = worldToCanvasCoordinates(entity.getPosition());


            double diameter = worldToCanvasLength(10);
            drawLight(pos.x, pos.y, worldToCanvasLength(15), getSideColor(entity), 0.25);
            foreground.fillOval(pos.x - diameter / 2, pos.y - diameter / 2, diameter, diameter);
            foreground.setFont(Font.font("sans-serif", 15));
            foreground.setFill(Color.BLACK);
            foreground.fillText(((Base) entity).getName(), pos.x + diameter, pos.y);

            double test = worldToCanvasLength(500);
            foreground.setLineWidth(2);
            foreground.strokeOval(pos.x - test / 2, pos.y - test / 2, test, test);
        });
    }

    private void rotate(GraphicsContext gc, double angle, double px, double py) {
        Rotate r = new Rotate(angle, px, py);
        gc.setTransform(r.getMxx(), r.getMyx(), r.getMxy(), r.getMyy(), r.getTx(), r.getTy());
    }

    private void drawBackground() {
        final Vector2D worldTopLeft = worldToCanvasCoordinates(Vector2D.ZERO());
        final Vector2D worldBottomRight = worldToCanvasCoordinates(new Vector2D(worldWidth.get(), worldHeight.get()));

        background.setStroke(Color.gray(0.3));

        final Vector2D canvasTopLeft = canvasToWorldCoordinates(Vector2D.ZERO());
        final Vector2D canvasBottomRight = canvasToWorldCoordinates(new Vector2D(getWidth(), getHeight()));

        final double zoom = this.zoom.get();
        final double gridSpacing = 40 / Math.pow(2, Math.floor(Math.log(zoom)));
        final int majorGrid = 4;

        final double startX = canvasTopLeft.x - (canvasTopLeft.x % (gridSpacing * majorGrid) + gridSpacing * majorGrid);
        for (int i = 0; i < (canvasBottomRight.x - startX) / gridSpacing; i++) {
            Vector2D canvas = worldToCanvasCoordinates(new Vector2D(startX + gridSpacing * i, 0));
            if (i % majorGrid == 0) {
                background.setLineWidth(3);
            } else {
                background.setLineWidth(1);
            }
            background.strokeLine(canvas.x, 0, canvas.x, this.getHeight());
        }

        final double startY = canvasTopLeft.y - (canvasTopLeft.y % (gridSpacing * majorGrid) + gridSpacing * majorGrid);
        for (int i = 0; i < (canvasBottomRight.y - startY) / gridSpacing; i++) {
            Vector2D canvas = worldToCanvasCoordinates(new Vector2D(0, startY + gridSpacing * i));
            if (i % majorGrid == 0) {
                background.setLineWidth(3);
            } else {
                background.setLineWidth(1);
            }
            background.strokeLine(0, canvas.y, this.getWidth(), canvas.y);

        }


        final float borderWidth = 4;
        background.setFill(Color.PAPAYAWHIP);
        background.fillRect(worldTopLeft.x - borderWidth, worldTopLeft.y - borderWidth, worldBottomRight.x - worldTopLeft.x + borderWidth * 2, worldBottomRight.y - worldTopLeft.y + borderWidth * 2);
        background.drawImage(backgroundImage, worldTopLeft.x, worldTopLeft.y, worldBottomRight.x - worldTopLeft.x, worldBottomRight.y - worldTopLeft.y);
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

            render();
        }
    };


    boolean backgroundMustRerender = true;

    private void render() {
        ArrayList<Entity> entities = worldModel.get().getEntities().stream().filter(Objects::nonNull).collect(Collectors.toCollection(ArrayList::new));

        foreground.clearRect(0, 0, getWidth(), getHeight());
        light.clearRect(0, 0, getWidth(), getHeight());

        if (backgroundMustRerender) {
            background.clearRect(0, 0, getWidth(), getHeight());
            backgroundMustRerender = false;
            drawBackground();
        }
        drawEntities(entities);
        drawStats(entities);
    }

    private void drawStats(List<Entity> entities) {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("E: %d; \n", entities.size()));
        sb.append(String.format("T: %.2f; \n", entities.size() > 0 ? entities.get(0).getWorld().getCurrentTime() : 0));
        sb.append(String.format("F: %.2f; \n", 1000000000 / smoothedFrameTime));
        sb.append(String.format("f: %.2fms; \n", lastFrameTime / 1000000d));
        sb.append(String.format("S: %.2f; \n", entities.size() > 0 ? entities.get(0).getWorld().getSimulationSpeed() : 0));
        sb.append(String.format("U: %d; \n", entities.size() > 0 ? entities.get(0).getWorld().getUpdateInterval() : 0));

        foreground.setTextBaseline(VPos.TOP);
        foreground.setFill(Color.PAPAYAWHIP);
        foreground.fillText(sb.toString(), 5, 5);

    }
}
