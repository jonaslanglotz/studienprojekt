package main.java.model.world;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import main.java.model.Vector2D;
import main.java.model.WorldModel;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;

/**
 * Base class for all entities contained in a world
 */
public class Entity {
    /**
     * The instant in world time when this entity was created.
     */
    @Getter
    public final double creationTime;

    /**
     * The id of this entity.
     */
    @Getter
    protected int id;


    /**
     * The sie this entity belongs to.
     */
    @Getter
    @Setter
    protected Side side;

    @Getter
    @Setter
    protected boolean isDestroyed = false;

    @Getter
    @Setter
    protected boolean willBeDestroyed = false;

    @Getter
    protected double destructionTime;

    /**
     * The world this entity exists in.
     */
    @Getter
    protected WorldModel world;
    /**
     * Position of the entity in world coordinates.
     */
    @Getter
    protected Vector2D position;
    protected final PropertyChangeSupport changes = new PropertyChangeSupport(this);

    /**
     * @param world    Position of the entity in world coordinates.
     * @param position The world this entity exists in.
     */
    public Entity(@NonNull WorldModel world, @NonNull Vector2D position, Side side) {
        this.world = world;
        this.position = position;

        this.creationTime = this.world.getCurrentTime();
        this.id = world.getNewId();
        this.side = side;
    }

    /**
     * Prepares this object for deletion by removing references to other objects.
     */
    public void destruct() {
        if (isDestroyed) {
            return;
        }
        destructionTime = world.getCurrentTime();
        isDestroyed = true;
        Arrays.stream(changes.getPropertyChangeListeners())
                .forEach(changes::removePropertyChangeListener);
    }

    /**
     * Position of the entity in world coordinates.
     *
     * @param position The new value.
     */
    public void setPosition(@NonNull Vector2D position) {
        final Vector2D oldValue = this.position;
        this.position = position;
        changes.firePropertyChange("position", oldValue, position);
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        changes.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        changes.removePropertyChangeListener(l);
    }
}
