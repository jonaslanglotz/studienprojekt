package main.java.model;

import lombok.Getter;
import lombok.NonNull;
import main.java.model.world.Entity;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;
import java.util.stream.Collectors;

public class DefaultWorldModel implements WorldModel, PropertyChangeListener {

    /**
     * Counts up as new entities are created.
     */
    private int idCounter;

    @Getter
    private final Timer timer = new Timer(true);


    private long defaultUpdateIntervalMs;
    private double smoothedUpdateIntervalMs = 0;
    private double resourceUsage = 1;

    public void reportUpdateIntervalNs(long intervalNs) {
        double smoothing = 0.9;
        smoothedUpdateIntervalMs = (smoothedUpdateIntervalMs * smoothing) + ((intervalNs * 0.8 / 1000000.0) * (1.0 - smoothing));
        resourceUsage = smoothedUpdateIntervalMs / defaultUpdateIntervalMs;
        setMaxSafeSimulationSpeed(wantedSimulationSpeed / resourceUsage);
    }

    public long getUpdateInterval() {
        return (long) (entities.size() == 0 ? defaultUpdateIntervalMs : defaultUpdateIntervalMs * resourceUsage);
    }

    /**
     * The width of the world.
     */
    @Getter
    public final double width;

    /**
     * The height of the world.
     */
    @Getter
    public final double height;

    /**
     * A list of all entities in the world.
     */
    private final List<Entity> entities = Collections.synchronizedList(new ArrayList<>());

    /**
     * The speed the simulation should run at.
     */
    @Getter
    private double wantedSimulationSpeed;

    /**
     * The maximum simulation speed the simulation may run at, given the resource limits.
     */
    @Getter
    private double maxSafeSimulationSpeed = Double.POSITIVE_INFINITY;

    /**
     * The nanosecond timestamp of the last time update.
     */
    private long lastUpdateNs;

    /**
     * The internal float timestamp of the last time update.
     */
    private double lastUpdateInternalTime;

    private PropertyChangeSupport changes = new PropertyChangeSupport(this);

    public DefaultWorldModel(double width, double height, double wantedSimulationSpeed, long defaultUpdateIntervalMs) {
        this.width = width;
        this.height = height;
        this.wantedSimulationSpeed = wantedSimulationSpeed;
        this.defaultUpdateIntervalMs = defaultUpdateIntervalMs;

        this.lastUpdateNs = System.nanoTime();
        this.lastUpdateInternalTime = 0;
    }

    /**
     * @return A list of all entities in the world.
     */
    @Override
    public List<Entity> getEntities() {
        return new ArrayList<>(entities);
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
        WorldModel outerThis = this;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                entities.remove(entity);
                changes.firePropertyChange("entities", null, outerThis.getEntities());
            }
        }, 10000);
    }


    /**
     * Calculate and set the current time, based on simulation speed and how many milliseconds have passed since the last update
     */
    private void updateCurrentTime() {
        long nowNs = System.nanoTime();
        long nsSinceLastUpdate = nowNs - lastUpdateNs;
        double passedInternalTime = nsSinceLastUpdate * getSimulationSpeed() / 1000000000.0;

        lastUpdateInternalTime += passedInternalTime;
        lastUpdateNs = nowNs;
    }

    /**
     * @return The current internal timestamp.
     */
    @Override
    public double getCurrentTime() {
        updateCurrentTime();
        return lastUpdateInternalTime;
    }

    @Override
    public double getSimulationSpeed() {
        return Math.min(wantedSimulationSpeed, maxSafeSimulationSpeed);
    }

    /**
     * The speed the simulation should run at. A value of 1.0 represents real-time.
     *
     * @param wantedSimulationSpeed The new value.
     */
    @Override
    public void setWantedSimulationSpeed(double wantedSimulationSpeed) {
        final double previousSimulationSpeed = getSimulationSpeed();

        final double oldValue = this.wantedSimulationSpeed;
        updateCurrentTime();
        this.wantedSimulationSpeed = wantedSimulationSpeed;
        changes.firePropertyChange("wantedSimulationSpeed", oldValue, wantedSimulationSpeed);

        changes.firePropertyChange("simulationSpeed", previousSimulationSpeed, getSimulationSpeed());
    }

    /**
     * @param maxSafeSimulationSpeed The new value.
     */
    private void setMaxSafeSimulationSpeed(double maxSafeSimulationSpeed) {
        final double previousSimulationSpeed = getSimulationSpeed();

        final double oldValue = this.maxSafeSimulationSpeed;
        updateCurrentTime();
        this.maxSafeSimulationSpeed = maxSafeSimulationSpeed;
        changes.firePropertyChange("maxSafeSimulationSpeed", oldValue, maxSafeSimulationSpeed);
       
        changes.firePropertyChange("simulationSpeed", previousSimulationSpeed, getSimulationSpeed());
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
        return getEntities().stream().filter(entity -> entity.getId() == id).findFirst().orElse(null);
    }

    @Override
    public <T> List<T> getEntitiesByType(Class<T> clazz) {
        return getEntities().stream().filter(clazz::isInstance).map(entity -> (T) entity).collect(Collectors.toList());
    }

    public List<Entity> getEntitiesByPosition(Vector2D position, double radius) {
        return getEntitiesByPosition(position, radius, false);
    }

    public List<Entity> getEntitiesByPosition(Vector2D position, double radius, boolean includeDestroyed) {

        HashMap<Vector2D, Entity> map = new HashMap<>();

        getEntities().stream().filter(entity -> includeDestroyed || !entity.isDestroyed()).forEach(entity -> {
            if (entity == null || entity.isDestroyed()) {
                return;
            }
            Vector2D difference = entity.getPosition().sub(position);
            if (difference.length() < radius) {
                map.put(entity.getPosition(), entity);
            }
        });

        ArrayList<Vector2D> sortedPositions = map.keySet().stream().sorted((o1, o2) -> {
            Double distance1 = o1.distanceTo(position);
            Double distance2 = o2.distanceTo(position);
            return distance1.compareTo(distance2);
        }).collect(Collectors.toCollection(ArrayList::new));

        return sortedPositions.stream().map(map::get).collect(Collectors.toCollection(ArrayList::new));
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
