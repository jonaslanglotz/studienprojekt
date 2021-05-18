package main.java.model.world.rockets;

import main.java.model.WorldModel;

import javax.vecmath.Vector2f;

public class SimpleInterceptorRocket extends Rocket {

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
    public SimpleInterceptorRocket(WorldModel world, Vector2f position, int updateInterval, float errorRate, Vector2f startPosition, Vector2f velocity, float steerRate, Rocket targetRocket) {
        super(world, position, updateInterval, errorRate, targetRocket.getPosition(), startPosition, velocity, steerRate);
        this.targetRocket = targetRocket;
    }

    /**
     * Perform one simulation step for this rocket.
     *
     * The simple interceptor rocket continually steers towards an incoming rocket to intercept it.
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
