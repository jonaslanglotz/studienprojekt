package main.java.view.main;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import main.java.model.world.Entity;

public class MapView extends Canvas {

    private ListProperty<Entity> entities;

    public MapView (double width, double height) {
       super(width, height);
       init();
    }

    public MapView () {
        super();
        init();
    }

    private void init() {
        entities = new SimpleListProperty<Entity>();
        entities.addListener((ListChangeListener<? super Entity>) c -> render());
    }

    private void render() {
        GraphicsContext gc = this.getGraphicsContext2D();
        gc.clearRect(0,0, getWidth(), getHeight());
        gc.setFill(Color.RED);
        gc.fillOval(500, 500, 5, 5);

        for (Entity entity : entities) {
            gc.setStroke(Color.PINK);
            gc.setLineWidth(4);
            gc.strokeOval(entity.getPosition().x, entity.getPosition().y, 5, 5);
        }
    }

    public void setEntityList (ObservableList<Entity> entities) {
        this.entities.set(entities);
    }

    public ObservableList<Entity> getEntityList () {
        return entities.get();
    }

    public ListProperty<Entity> getEntities () {
        return entities;
    }

}
