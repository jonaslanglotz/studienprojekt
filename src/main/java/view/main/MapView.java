package main.java.view.main;

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
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.transform.Rotate;
import lombok.Getter;
import main.java.model.world.Base;
import main.java.model.world.Entity;
import main.java.model.world.rockets.Rocket;

import javax.vecmath.Vector2f;
import java.util.ArrayList;

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

    MapViewUpdate lastUpdate;

    public MapView(double width, double height) {
        super(width, height);
        init();
    }

    public MapView() {
        super();
        init();
    }

    private void init() {
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

        backgroundImage = new Image("map_3.jpg");

        render();
    }

    private float worldToCanvasLength(float worldLength) {
        return (float) (worldLength * this.zoom.getValue());
    }

    private Vector2f worldToCanvasCoordinates(Vector2f worldCoordinates) {
        final float centerWorldX = this.centerWorldX.floatValue();
        final float centerWorldY = this.centerWorldY.floatValue();
        final float worldWidth = this.worldWidth.floatValue();
        final float worldHeight = this.worldHeight.floatValue();
        final float zoom = this.zoom.floatValue();

        final float canvasWidth = (float) this.getWidth();
        final float canvasHeight = (float) this.getHeight();

        final float sectionWorldWidth = canvasWidth / zoom;
        final float sectionWorldHeight = canvasHeight / zoom;
        final Vector2f sectionTopLeft = new Vector2f(centerWorldX - 0.5f * (sectionWorldWidth), centerWorldY - 0.5f * (sectionWorldHeight));

        return new Vector2f(
                (worldCoordinates.x - sectionTopLeft.x) / sectionWorldWidth * canvasWidth,
                (worldCoordinates.y - sectionTopLeft.y) / sectionWorldHeight * canvasHeight);
    }

    private Vector2f canvasToWorldCoordinates(Vector2f canvasCoordinates) {
        final float centerWorldX = this.centerWorldX.floatValue();
        final float centerWorldY = this.centerWorldY.floatValue();
        final float worldWidth = this.worldWidth.floatValue();
        final float worldHeight = this.worldHeight.floatValue();
        final float zoom = this.zoom.floatValue();

        final float canvasWidth = (float) this.getWidth();
        final float canvasHeight = (float) this.getHeight();

        final float sectionWorldWidth = canvasWidth / zoom;
        final float sectionWorldHeight = canvasHeight / zoom;
        final Vector2f sectionTopLeft = new Vector2f(centerWorldX - 0.5f * (sectionWorldWidth), centerWorldY - 0.5f * (sectionWorldHeight));

        return new Vector2f(
                sectionTopLeft.x + sectionWorldWidth * (canvasCoordinates.x / canvasWidth),
                sectionTopLeft.y + sectionWorldHeight * (canvasCoordinates.y / canvasHeight));
    }

    private void drawEntities() {
        GraphicsContext gc = this.getGraphicsContext2D();
        if (entities == null) {
            return;
        }

        ArrayList<Entity> entities = new ArrayList<>(this.entities.getValue());

        for (Entity entity : entities) {
            if (entity == null || entity.isDestroyed()) {
                continue;
            }

            final Vector2f pos = worldToCanvasCoordinates(entity.getPosition());

            switch (entity.getSide()) {
                case ATTACKER -> {
                    gc.setFill(attackerColor);
                    gc.setStroke(attackerColor);
                }
                case DEFENDER -> {
                    gc.setFill(defenderColor);
                    gc.setStroke(defenderColor);
                }
            }

            if (entity instanceof Rocket) {
                gc.setLineWidth(worldToCanvasLength(1));
                gc.save();

                Vector2f velocity = ((Rocket) entity).getVelocity();
                Vector2f up = new Vector2f(0, -1);

                float angle = (float) ((float) Math.atan2(velocity.x * up.y - velocity.y * up.x,
                        velocity.x * up.x + velocity.y * up.y) / Math.PI * -180);

                rotate(gc, angle, pos.x, pos.y);
                double[] xPoints = {
                        pos.x - worldToCanvasLength(2),
                        pos.x,
                        pos.x + worldToCanvasLength(2)
                };
                double[] yPoints = {
                        pos.y + worldToCanvasLength(4),
                        pos.y - worldToCanvasLength(2),
                        pos.y + worldToCanvasLength(4)
                };
                gc.strokePolyline(xPoints, yPoints, 3);
                gc.restore();

            }
            if (entity instanceof Base) {
                float diameter = worldToCanvasLength(10);
                gc.fillOval(pos.x - diameter / 2, pos.y - diameter / 2, diameter, diameter);
                gc.setFont(Font.font("sans-serif", 15));
                gc.setFill(Color.BLACK);
                gc.fillText(((Base) entity).getName(), pos.x + diameter, pos.y);

                float test = worldToCanvasLength(500);
                gc.strokeOval(pos.x - test / 2, pos.y - test / 2, test, test);
            }
        }

    }

    private void rotate(GraphicsContext gc, double angle, double px, double py) {
        Rotate r = new Rotate(angle, px, py);
        gc.setTransform(r.getMxx(), r.getMyx(), r.getMxy(), r.getMyy(), r.getTx(), r.getTy());
    }

    private void drawBackground() {
        GraphicsContext gc = this.getGraphicsContext2D();

        final Vector2f worldTopLeft = worldToCanvasCoordinates(new Vector2f(0, 0));
        final Vector2f worldBottomRight = worldToCanvasCoordinates(new Vector2f(worldWidth.floatValue(), worldHeight.floatValue()));

        gc.setStroke(Color.gray(0.3));

        final Vector2f canvasTopLeft = canvasToWorldCoordinates(new Vector2f(0, 0));
        final Vector2f canvasBottomRight = canvasToWorldCoordinates(new Vector2f((float) getWidth(), (float) getHeight()));

        final float zoom = this.zoom.floatValue();
        final float gridSpacing = (float) (40 / Math.pow(2, Math.floor(Math.log(zoom))));
        final float majorGrid = 4;

        final float startX = canvasTopLeft.x - (canvasTopLeft.x % (gridSpacing * majorGrid) + gridSpacing * majorGrid);
        for (float i = 0; i < (canvasBottomRight.x - startX) / gridSpacing; i++) {
            Vector2f canvas = worldToCanvasCoordinates(new Vector2f(startX + gridSpacing * i, 0));
            if (i % majorGrid == 0) {
                gc.setLineWidth(3);
            } else {
                gc.setLineWidth(1);
            }
            gc.strokeLine(canvas.x, 0, canvas.x, this.getHeight());
        }

        final float startY = canvasTopLeft.y - (canvasTopLeft.y % (gridSpacing * majorGrid) + gridSpacing * majorGrid);
        for (float i = 0; i < (canvasBottomRight.y - startY) / gridSpacing; i++) {
            Vector2f canvas = worldToCanvasCoordinates(new Vector2f(0, startY + gridSpacing * i));
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

    private void render(boolean isEntityUpdate) {
        long start = System.nanoTime();

        GraphicsContext gc = this.getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());

        drawBackground();
        drawEntities();
        drawStats();

        long finish = System.nanoTime();

        lastUpdate = new MapViewUpdate(start, finish);
    }

    private void drawStats() {
        GraphicsContext gc = this.getGraphicsContext2D();

        StringBuilder sb = new StringBuilder();

        sb.append(String.format("E: %d; ", entities.size()));

        gc.setTextBaseline(VPos.TOP);
        gc.fillText(sb.toString(), 5, 5);

    }

    public void setEntityList(ObservableList<Entity> entities) {
        this.entities.set(entities);
    }

    public ObservableList<Entity> getEntityList() {
        return entities.get();
    }
}
