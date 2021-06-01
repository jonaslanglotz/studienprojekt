package main.java.model.world.rockets;

import main.java.model.WorldModel;

import javax.vecmath.Vector2f;

public class FlakRocket extends Rocket {

    /**
     * @param world          The world this entity is spawned in
     * @param position       The position in the world the entity is spawned at
     * @param updateInterval
     * @param targetPosition
     * @param velocity
     */
    public FlakRocket(WorldModel world, Vector2f position, int updateInterval,
                      Vector2f targetPosition, Vector2f velocity) {
        super(world, position, updateInterval, 0, targetPosition, velocity, 0);
    }

    /**
     * @return true if the rocket has reached the target position or is ahead of it. False if it has not yet done so.
     */
    @Override
    protected boolean shouldExplode() {
        // TODO Add logic
        return false;
    }
}
