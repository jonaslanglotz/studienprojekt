package main.java.model;

import main.java.model.world.Entity;

import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Timer;

public interface WorldModel {

    /**
     * @return A list of all entities in the world.
     */
    List<Entity> getEntities();

    /**
     * @return A timer for scheduling stuff
     */
    Timer getTimer();


    public void reportUpdateIntervalNs(long intervalNs);

    public long getUpdateInterval();

    /**
     * Spawn an entity into the world.
     *
     * @param entity The entity to spawn.
     */
    void spawn(Entity entity);

    /**
     * Delete an entity from the world.
     *
     * @param entity The entity to delete.
     */
    void destroy(Entity entity);

    /**
     * @return The width of the world.
     */
    double getWidth();

    /**
     * @return The height of the world.
     */
    double getHeight();

    /**
     * @return The current internal timestamp.
     */
    double getCurrentTime();

    /**
     * @return The speed the simulation runs at. A value of 1.0 represents real-time.
     */
    double getSimulationSpeed();

    double getWantedSimulationSpeed();

    void setWantedSimulationSpeed(double wantedSimulationSpeed);

    void addPropertyChangeListener(PropertyChangeListener l);

    void removePropertyChangeListener(PropertyChangeListener l);

    int getNewId();

    Entity getEntityById(int id);

    public List<Entity> getEntitiesByPosition(Vector2D position, double radius);

    <T> List<T> getEntitiesByType(Class<T> clazz);
}
