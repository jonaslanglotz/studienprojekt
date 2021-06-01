package main.java.model.world;

import lombok.Getter;
import lombok.Setter;
import main.java.model.WorldModel;

import javax.vecmath.Vector2f;
import java.util.Timer;
import java.util.TimerTask;

/**
 * An entity that periodically updates itself by an update method.
 */
public abstract class DynamicEntity extends Entity implements Runnable {

    /**
     * The timer used to schedule update iterations.
     */
    protected final Timer timer = new Timer(true);
    /**
     * The amount of milliseconds between each execution of the update loop.
     */
    @Getter
    @Setter
    protected int updateInterval;

    /**
     * @param world          Position of the entity in world coordinates.
     * @param position       The world this entity exists in.
     * @param updateInterval The amount of milliseconds between each execution of the update loop.
     */
    public DynamicEntity(WorldModel world, Vector2f position, int updateInterval) {
        super(world, position);
        this.updateInterval = updateInterval;
        this.run();
    }

    /**
     * Prepares this object for deletion by cancelling all future executions of the update method and
     * calling the parent method {@link Entity#destruct()}
     */
    @Override
    public void destruct() {
        timer.cancel();
        super.destruct();
    }

    @Override
    public void run() {
        timer.scheduleAtFixedRate(
                new TimerTask() {
                    @Override
                    public void run() {
                        update();
                    }
                },
                0,
                updateInterval);
    }

    /**
     * The method to be called at regular intervals.
     */
    protected abstract void update();
}
