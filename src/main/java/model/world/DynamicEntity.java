package main.java.model.world;

import main.java.model.Vector2D;
import main.java.model.WorldModel;

import java.util.TimerTask;

/**
 * An entity that periodically updates itself by an update method.
 */
public abstract class DynamicEntity extends Entity implements Runnable {

    protected double lastUpdateTime;

    /**
     * @param world    Position of the entity in world coordinates.
     * @param position The world this entity exists in.
     */
    public DynamicEntity(WorldModel world, Vector2D position, Side side) {
        super(world, position, side);
        this.run();
    }

    /**
     * Prepares this object for deletion by cancelling all future executions of the update method and
     * calling the parent method {@link Entity#destruct()}
     */
    @Override
    public void destruct() {
        super.destruct();
    }

    long lastUpdate = 0;

    public TimerTask newUpdateTask() {
        DynamicEntity outerThis = this;
        return new TimerTask() {
            @Override
            public void run() {
                if (willBeDestroyed && !isDestroyed) {
                    world.destroy(outerThis);
                    return;
                }

                if (!isDestroyed) {
                    final double currentTime = world.getCurrentTime();
                    double deltaTime = currentTime - lastUpdateTime;
                    lastUpdateTime = currentTime;
                    update(deltaTime);

                }

                long now = System.nanoTime();
                world.reportUpdateIntervalNs(now - lastUpdate);
                world.getTimer().schedule(newUpdateTask(), world.getUpdateInterval());
                lastUpdate = now;
            }
        };
    }

    @Override
    public void run() {
        world.getTimer().schedule(newUpdateTask(), world.getUpdateInterval());
        lastUpdate = System.nanoTime();
    }

    /**
     * The method to be called at regular intervals.
     *
     * @param deltaTime
     */
    protected abstract void update(double deltaTime);
}
