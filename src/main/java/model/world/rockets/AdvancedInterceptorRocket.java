package main.java.model.world.rockets;

import main.java.model.WorldModel;

import javax.vecmath.Vector2f;

public class AdvancedInterceptorRocket extends Rocket {

    private Rocket targetRocket;

    /**
     * @param world          The world this entity is spawned in
     * @param position       The position in the world the entity is spawned at
     * @param updateInterval
     * @param errorRate
     * @param startPosition
     * @param velocity
     * @param steerRate
     */
    public AdvancedInterceptorRocket(WorldModel world, Vector2f position, int updateInterval, float errorRate, Vector2f startPosition, Vector2f velocity, float steerRate, Rocket targetRocket) {
        super(world, position, updateInterval, errorRate, targetRocket.getPosition(), startPosition, velocity, steerRate);
        this.targetRocket = targetRocket;
    }

    /**
     * Perform one simulation step for this rocket.
     *
     * The advanced interceptor rocket tries to predict the future path of an incoming rocket, and intercepting a predicted future point in space.
     */
    @Override
    protected void update() {
        // TODO Add logic
    }

    /**
     * @return true if the target rocket is near or has been missed. False if otherwise.
     */
    @Override
    protected boolean shouldExplode() {
        // TODO Add logic
        return false;
    }
}
