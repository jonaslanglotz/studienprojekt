package main.java.model.world;

import main.java.model.WorldModel;
import main.java.model.world.Entity;

import javax.vecmath.Vector2f;
import java.util.Timer;
import java.util.TimerTask;

public abstract class DynamicEntity extends Entity implements Runnable {

    private Timer timer = new Timer(true);
    private int updateInterval;

    /**
     * @param world    The world this entity is spawned in
     * @param position The position in the world the entity is spawned at
     */
    public DynamicEntity(WorldModel world, Vector2f position, int updateInterval) {
        super(world, position);
        this.updateInterval = updateInterval;
        this.run();
    }

    public void stop() {
        timer.cancel();
    }

    @Override
    public void run() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                update();
            }
        }, 0, updateInterval);
    }

    protected abstract void update();
}
