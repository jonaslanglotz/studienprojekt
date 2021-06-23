package main.java.model;

import main.java.model.world.Entity;

import javax.vecmath.Vector2f;
import java.beans.PropertyChangeListener;
import java.util.List;

public interface WorldModel {

    /**
     * @return A list of all entities in the world.
     */
    List<Entity> getEntities();

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
    float getWidth();

    /**
     * @return The height of the world.
     */
    float getHeight();

    /**
     * @return The current internal timestamp.
     */
    float getCurrentTime();

    /**
     * @return The speed the simulation runs at. A value of 1.0 represents real-time.
     */
    float getSimulationSpeed();

    /**
     * The speed the simulation runs at. A value of 1.0 represents real-time.
     *
     * @param simulationSpeed The new value.
     */
    void setSimulationSpeed(float simulationSpeed);

    void addPropertyChangeListener(PropertyChangeListener l);

    void removePropertyChangeListener(PropertyChangeListener l);

    int getNewId();

    Entity getEntityById(int id);

    public List<Entity> getEntitiesByPosition(Vector2f position, float radius);

    <T> List<T> getEntitiesByType(Class<T> clazz);
}
