package main.java.model;

import java.beans.PropertyChangeListener;

import javafx.collections.ObservableList;
import main.java.model.world.Entity;

public interface WorldModel{
    ObservableList<Entity> getEntities();
    void spawnEntity();

    float getWidth();
    float setWidth();

    float getHeight();
    float setHeight();

    int getCurrentTime();

    void addPropertyChangeListener(PropertyChangeListener l);
    void removePropertyChangeListener(PropertyChangeListener l);

}
