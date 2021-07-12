package main.java.model.world.rockets;

import lombok.Getter;
import lombok.NonNull;
import main.java.model.Vector2D;
import main.java.model.WorldModel;
import main.java.model.world.DynamicEntity;
import main.java.model.world.Side;
import main.java.model.world.SimplexNoise;

public class Rocket extends DynamicEntity {

    @Getter
    final protected Vector2D startPosition;
    @Getter
    protected double errorStrength;
    @Getter
    protected Vector2D targetPosition;
    @Getter
    protected Vector2D velocity;

    @Getter
    protected double steerRate;

    /**
     * @param world          The world this entity is spawned in
     * @param position       The position in the world the entity is spawned at
     * @param updateInterval The amount of milliseconds between each execution of the update loop.
     * @param errorStrength  The amount the actual steering angle may differ from the planned steering angle.
     * @param targetPosition The position of the target the rocket is trying to hit.
     * @param velocity       The vector representation of the rockets movement.
     * @param steerRate      The maximum angle a rocket can turn per second.
     */
    public Rocket(WorldModel world, Vector2D position, Side side, int updateInterval, double errorStrength,
                  @NonNull Vector2D targetPosition, @NonNull Vector2D velocity, double steerRate) {
        super(world, position, side);
        this.startPosition = position;
        this.errorStrength = errorStrength;
        this.targetPosition = targetPosition;
        this.velocity = velocity;
        this.steerRate = steerRate;
        this.lastUpdateTime = world.getCurrentTime();
    }

    /**
     * @param world          The world this entity is spawned in
     * @param position       The position in the world the entity is spawned at
     * @param updateInterval The amount of milliseconds between each execution of the update loop.
     * @param errorStrength  The amount the actual steering angle may differ from the planned steering angle.
     * @param targetPosition The position of the target the rocket is trying to hit.
     * @param speed          The speed the rocket should fly at
     * @param steerRate      The maximum angle a rocket can turn per second.
     */
    public Rocket(WorldModel world, Vector2D position, Side side, int updateInterval, double errorStrength,
                  @NonNull Vector2D targetPosition, double speed, double steerRate) {
        this(world, position, side, updateInterval, errorStrength, targetPosition, calculateVelocity(position, targetPosition, speed), steerRate);
    }

    static private Vector2D calculateVelocity(Vector2D position, Vector2D targetPosition, double speed) {
        Vector2D deltaPosition = targetPosition.sub(position);
        return deltaPosition.normalize().scale(speed);
    }

    public void setErrorStrength(double errorStrength) {
        final double oldValue = this.errorStrength;
        this.errorStrength = errorStrength;
        changes.firePropertyChange("errorStrength", oldValue, errorStrength);
    }

    public void setSteerRate(double steerRate) {
        final double oldValue = this.steerRate;
        this.steerRate = steerRate;
        changes.firePropertyChange("steerRate", oldValue, steerRate);
    }

    public void setTargetPosition(@NonNull Vector2D targetPosition) {
        final Vector2D oldValue = this.targetPosition;
        this.targetPosition = targetPosition;
        changes.firePropertyChange("targetPosition", oldValue, targetPosition);
    }

    public void setVelocity(@NonNull Vector2D velocity) {
        final Vector2D oldValue = this.velocity;
        this.velocity = velocity;
        changes.firePropertyChange("velocity", oldValue, velocity);
    }

    protected void update(double deltaTime) {

        Vector2D targetDirection = targetPosition.sub(position);
        double speed = velocity.length();

        double wantedTurnAngle = velocity.signedAngle(targetDirection);
        double maximumAngle = steerRate * deltaTime;
        double turnAngle = Math.max(-maximumAngle, Math.min(maximumAngle, wantedTurnAngle));

        // Apply random offset to turning angle
        final double noiseScale = 1;
        final double offset = id * 100.0;
        turnAngle += SimplexNoise.noise((position.x + offset) * (noiseScale / speed), (position.y + offset) * (noiseScale / speed))
                * errorStrength * deltaTime;

        Vector2D newVelocity = velocity.rotate(turnAngle);

        setVelocity(newVelocity);
        setPosition(position.add(newVelocity.scale(deltaTime)));

        if (shouldExplode() && !isDestroyed) {
            this.setWillBeDestroyed(true);
        }
    }

    /**
     * @return true if the rocket has reached the target position or after it has missed. False if it has not yet done so.
     */
    protected boolean shouldExplode() {
        Vector2D difference = targetPosition.sub(position);
        return difference.length() < 5;
    }
}
