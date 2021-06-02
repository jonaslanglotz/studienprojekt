package main.java.viewmodel;


import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import main.java.model.WorldModel;
import main.java.model.world.Entity;
import main.java.model.world.rockets.Rocket;

import javax.vecmath.Vector2f;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;

public class MainViewModel {
    WorldModel worldModel;

    private ListProperty<Entity> entities;

    public MainViewModel(WorldModel worldModel) {
        this.worldModel = worldModel;

        entities = new SimpleListProperty<>();

        this.worldModel.addPropertyChangeListener(evt -> Platform.runLater(() -> updateValues(evt)));
    }

    public void updateValues(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case "entities":
                //entities.set(new ObservableList<Entity>(evt.getNewValue()));
                if (evt.getNewValue() instanceof ArrayList) {
                    this.entities.set(FXCollections.observableArrayList((ArrayList<Entity>) evt.getNewValue()));
                }
                break;

            default:
                break;
        }
    }

    public ListProperty<Entity> getEntities() {
        return entities;
    }

    public void spawnEntity() {
        for (int i = 0; i < 10; i++) {
            Entity entity = new Rocket(
                    worldModel,
                    new Vector2f((float) Math.random() * 100, (float) Math.random() * 100),
                    20,
                    0f,
                    new Vector2f(500, 500),
                    new Vector2f(50, 10),
                    1
            );
            worldModel.spawn(entity);
        }
    }
}
