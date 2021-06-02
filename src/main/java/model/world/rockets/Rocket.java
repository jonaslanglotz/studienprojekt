package main.java.model.world.rockets;

import lombok.Getter;
import lombok.NonNull;
import main.java.model.WorldModel;
import main.java.model.world.DynamicEntity;

import javax.vecmath.Vector2f;

public class Rocket extends DynamicEntity {

    final protected Vector2f startPosition;
    @Getter
    protected float errorStrength;
    protected Vector2f targetPosition;
    protected Vector2f velocity;

    @Getter
    protected float steerRate;

    private float lastUpdateTime;

    /**
     * @param world          The world this entity is spawned in
     * @param position       The position in the world the entity is spawned at
     * @param updateInterval The amount of milliseconds between each execution of the update loop.
     * @param errorStrength  The amount the actual steering angle may differ from the planned steering angle.
     * @param targetPosition The position of the target the rocket is trying to hit.
     * @param velocity       The vector representation of the rockets movement.
     * @param steerRate      The maximum angle a rocket can turn per second.
     */
    public Rocket(WorldModel world, Vector2f position, int updateInterval, float errorStrength,
                  @NonNull Vector2f targetPosition, @NonNull Vector2f velocity, float steerRate) {
        super(world, position, updateInterval);
        this.startPosition = position;

        this.errorStrength = errorStrength;
        this.targetPosition = targetPosition;
        this.velocity = velocity;
        this.steerRate = steerRate;
        this.lastUpdateTime = world.getCurrentTime();
    }

    public void setErrorStrength(float errorStrength) {
        final float oldValue = this.errorStrength;
        this.errorStrength = errorStrength;
        changes.firePropertyChange("errorStrength", oldValue, errorStrength);
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
        // Calculate passed in-world time
        final float currentTime = world.getCurrentTime();
        float deltaTime = currentTime - lastUpdateTime;
        lastUpdateTime = currentTime;

        // Copy position and velocity values
        Vector2f newPosition = new Vector2f(position);
        Vector2f newVelocity = new Vector2f(velocity);

        // Calculate vector pointing to target
        Vector2f targetDirection = new Vector2f(targetPosition.x - position.x, targetPosition.y - position.y);

        // Calculate perp dot product (angle in radians to targetDirection from velocity)
        float wantedTurnAngle =
                (float) Math.atan2(velocity.x * targetDirection.y - velocity.y * targetDirection.x,
                        velocity.x * targetDirection.x + velocity.y * targetDirection.y);

        // Calculate actual turn angle based on steerRate and passed time
        float turnAngle;
        if (wantedTurnAngle <= 0) {
            turnAngle = Math.max(wantedTurnAngle, steerRate * deltaTime * -1);
        } else {
            turnAngle = Math.min(wantedTurnAngle, steerRate * deltaTime);
        }

        // Apply random offset to turning angle
        turnAngle += (Math.random() - 0.5f) * 2 * errorStrength * deltaTime;

        // Rotate the velocity vector by turnAngle
        newVelocity = new Vector2f(
                (float) (Math.cos(turnAngle) * newVelocity.x - Math.sin(turnAngle) * newVelocity.y),
                (float) (Math.sin(turnAngle) * newVelocity.x + Math.cos(turnAngle) * newVelocity.y));

        // Save new Velocity
        setVelocity(newVelocity);

        newVelocity = new Vector2f(newVelocity);

        // Apply time scaling to calculated velocity and apply it
        newVelocity.scale(deltaTime, velocity);
        newPosition.add(newVelocity);

        // Save new position
        setPosition(newPosition);

        // TODO make explode
    }

    /**
     * @return true if the rocket has reached the target position or after it has missed. False if it has not yet done so.
     */
    protected boolean shouldExplode() {
        // TODO Add logic
        return false;
    }
}
