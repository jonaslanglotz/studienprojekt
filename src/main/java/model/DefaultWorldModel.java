package main.java.model;

import lombok.Getter;
import lombok.NonNull;
import main.java.model.world.Entity;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultWorldModel implements WorldModel, PropertyChangeListener {

    /**
     * Counts up as new entities are created.
     */
    private int idCounter;

    /**
     * The width of the world.
     */
    @Getter
    public final float width;

    /**
     * The height of the world.
     */
    @Getter
    public final float height;

    /**
     * A list of all entities in the world.
     */
    private final ArrayList<Entity> entities = new ArrayList<>();

    /**
     * The speed the simulation runs at. A value of 1.0 represents real-time.
     */
    @Getter
    private float simulationSpeed;

    /**
     * The nanosecond timestamp of the last time update.
     */
    private long lastUpdateNs;

    /**
     * The internal float timestamp of the last time update.
     */
    private float lastUpdateInternalTime;

    private PropertyChangeSupport changes = new PropertyChangeSupport(this);

    public DefaultWorldModel(float width, float height, float simulationSpeed) {
        this.width = width;
        this.height = height;
        this.simulationSpeed = simulationSpeed;

        this.lastUpdateNs = System.nanoTime();
        this.lastUpdateInternalTime = 0;
    }

    /**
     * @return A list of all entities in the world.
     */
    @Override
    public List<Entity> getEntities() {
        return (ArrayList<Entity>) entities.clone();
    }

    /**
     * Spawn an entity into the world.
     *
     * @param entity The entity to spawn.
     */
    @Override
    public void spawn(@NonNull Entity entity) {
        final List<Entity> oldValue = this.getEntities();
        entities.add(entity);
        entity.addPropertyChangeListener(this);
        changes.firePropertyChange("entities", oldValue, this.getEntities());
    }

    /**
     * Delete an entity from the world.
     */
    @Override
    public void destroy(@NonNull Entity entity) {
        entity.destruct();
        if (entities.remove(entity)) {
            changes.firePropertyChange("entities", null, this.getEntities());
        }
    }


    /**
     * Calculate and set the current time, based on simulation speed and how many milliseconds have passed since the last update
     */
    private void updateCurrentTime() {
        long nowNs = System.nanoTime();
        long nsSinceLastUpdate = nowNs - lastUpdateNs;
        float passedInternalTime = nsSinceLastUpdate * simulationSpeed / 1000000000f;

        lastUpdateInternalTime += passedInternalTime;
        lastUpdateNs = nowNs;
    }

    /**
     * @return The current internal timestamp.
     */
    @Override
    public float getCurrentTime() {
        updateCurrentTime();
        return lastUpdateInternalTime;
    }

    /**
     * The speed the simulation runs at. A value of 1.0 represents real-time.
     *
     * @param simulationSpeed The new value.
     */
    @Override
    public void setSimulationSpeed(float simulationSpeed) {
        final float oldValue = this.simulationSpeed;
        updateCurrentTime();
        this.simulationSpeed = simulationSpeed;
        changes.firePropertyChange("simulationSpeed",oldValue, simulationSpeed);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        changes.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        changes.removePropertyChangeListener(l);
    }

    @Override
    public int getNewId() {
        idCounter++;
        return idCounter - 1;
    }

    @Override
    public Entity getEntityById(int id) {
        return entities.stream().filter(entity -> entity.getId() == id).findFirst().orElse(null);
    }

    @Override
    public <T> List<T> getEntitiesByType(Class<T> clazz) {
        return entities.stream().filter(clazz::isInstance).map(entity -> (T) entity).collect(Collectors.toList());
    }

    /**
     * This method gets called when a bound property is changed.
     *
     * @param evt A PropertyChangeEvent object describing the event source
     *            and the property that has changed.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        changes.firePropertyChange("entities", null, this.getEntities());
    }
}
