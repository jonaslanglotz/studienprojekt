package main.java.model.world.rockets;

import lombok.Getter;
import lombok.NonNull;
import main.java.model.WorldModel;
import main.java.model.world.DynamicEntity;

import javax.vecmath.Vector2f;

public class Rocket extends DynamicEntity {

    @Getter
    protected float errorRate;

    protected Vector2f targetPosition;

    final protected Vector2f startPosition;

    protected Vector2f velocity;

    @Getter
    protected float steerRate;

    /**
     * @param world          The world this entity is spawned in
     * @param position       The position in the world the entity is spawned at
     * @param updateInterval The amount of milliseconds between each execution of the update loop.
     * @param errorRate      The amount the actual steering angle may differ from the planned steering angle.
     * @param targetPosition The position of the target the rocket is trying to hit.
     * @param velocity       The vector representation of the rockets movement.
     * @param steerRate      The maximum angle a rocket can turn per second.
     */
    public Rocket(WorldModel world, Vector2f position, int updateInterval, float errorRate,
                  @NonNull Vector2f targetPosition, @NonNull Vector2f velocity, float steerRate) {
        super(world, position, updateInterval);
        this.startPosition = position;

        this.errorRate = errorRate;
        this.targetPosition = targetPosition;
        this.velocity = velocity;
        this.steerRate = steerRate;
    }

    public void setErrorRate(float errorRate) {
        final float oldValue = this.errorRate;
        this.errorRate = errorRate;
        changes.firePropertyChange("errorRate", oldValue, errorRate);
    }

    public void setSteerRate(float steerRate) {
        final float oldValue = this.steerRate;
        this.steerRate = steerRate;
        changes.firePropertyChange("steerRate", oldValue, steerRate);
    }

    public Vector2f getTargetPosition() {
        return (Vector2f) targetPosition.clone();
    }

    public void setTargetPosition(@NonNull Vector2f targetPosition) {
        final Vector2f oldValue = (Vector2f) this.targetPosition.clone();
        this.targetPosition = targetPosition;
        changes.firePropertyChange("targetPosition", oldValue, targetPosition);
    }

    public Vector2f getStartPosition() {
        return (Vector2f) startPosition.clone();
    }

    public Vector2f getVelocity() {
        return (Vector2f) velocity.clone();
    }

    public void setVelocity(@NonNull Vector2f velocity) {
        final Vector2f oldValue = (Vector2f) this.velocity.clone();
        this.velocity = velocity;
        changes.firePropertyChange("velocity", oldValue, velocity);
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
