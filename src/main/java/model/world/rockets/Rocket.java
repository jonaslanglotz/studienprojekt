package main.java.model.world.rockets;

import main.java.model.WorldModel;
import main.java.model.world.DynamicEntity;

import javax.vecmath.Vector2f;

public class Rocket extends DynamicEntity {

    private float errorRate;
    private Vector2f targetPosition;
    private Vector2f startPosition;
    private Vector2f velocity;
    private float steerRate;

    /**
     * @param world    The world this entity is spawned in
     * @param position The position in the world the entity is spawned at
     * @param updateInterval
     * @param errorRate
     * @param targetPosition
     * @param startPosition
     * @param velocity
     * @param steerRate
     */
    public Rocket(WorldModel world, Vector2f position, int updateInterval, float errorRate,
                  Vector2f targetPosition, Vector2f startPosition, Vector2f velocity, float steerRate) {
        super(world, position, updateInterval);

        this.errorRate = errorRate;
        this.targetPosition = targetPosition;
        this.startPosition = startPosition;
        this.velocity = velocity;
        this.steerRate = steerRate;
    }

    @Override
    public void run() {
        // TODO Add update loop
    }

    protected void update() {
        // TODO Add logic
    }

    /**
     * @return true if the rocket has reached the target position or after it has missed. False if it has not yet done so.
     */
    protected boolean shouldExplode() {
        // TODO Add logic
        return false;
    }
}
