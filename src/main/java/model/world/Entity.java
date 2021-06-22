package main.java.model.world;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import main.java.model.WorldModel;

import javax.vecmath.Vector2f;
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
    public final float creationTime;

    /**
     * The id of this entity.
     */
    @Getter
    protected int id;

    @Getter
    @Setter
    protected boolean isDestroyed = false;

    /**
     * The world this entity exists in.
     */
    @Getter
    protected WorldModel world;
    /**
     * Position of the entity in world coordinates.
     */
    @Getter
    protected Vector2f position;
    protected final PropertyChangeSupport changes = new PropertyChangeSupport(this);

    /**
     * @param world    Position of the entity in world coordinates.
     * @param position The world this entity exists in.
     */
    public Entity(@NonNull WorldModel world, @NonNull Vector2f position) {
        this.world = world;
        this.position = position;

        this.creationTime = this.world.getCurrentTime();
        this.id = world.getNewId();
    }

    /**
     * Prepares this object for deletion by removing references to other objects.
     */
    public void destruct() {
        if (isDestroyed) {
            return;
        }
        isDestroyed = true;
        Arrays.stream(changes.getPropertyChangeListeners())
                .forEach(changes::removePropertyChangeListener);
        world = null;
        position = null;
    }

    /**
     * Position of the entity in world coordinates.
     *
     * @param position The new value.
     */
    public void setPosition(@NonNull Vector2f position) {
        final Vector2f oldValue = this.position;
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
