package main.java.model.world.rockets;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import main.java.model.WorldModel;
import main.java.model.world.Entity;
import main.java.model.world.Side;
import main.java.model.world.SimplexNoise;

import javax.vecmath.Vector2f;
import java.util.List;

public class SimpleInterceptorRocket extends Rocket {

    @NonNull
    @Getter
    @Setter
    private Rocket targetRocket;

    /**
     * @param world          The world this entity is spawned in
     * @param position       The position in the world the entity is spawned at
     * @param updateInterval
     * @param errorRate
     * @param velocity
     * @param steerRate
     */
    public SimpleInterceptorRocket(WorldModel world, Vector2f position, Side side, int updateInterval, float errorRate, Vector2f velocity, float steerRate, @NonNull Rocket targetRocket) {
        super(world, position, side, updateInterval, errorRate, targetRocket.getPosition(), velocity, steerRate);
        this.targetRocket = targetRocket;
    }

    public SimpleInterceptorRocket(WorldModel world, Vector2f position, Side side, int updateInterval, float errorRate, float speed, float steerRate, @NonNull Rocket targetRocket) {
        super(world, position, side, updateInterval, errorRate, targetRocket.getPosition(), speed, steerRate);
        this.targetRocket = targetRocket;
    }

    /**
     * Perform one simulation step for this rocket.
     * <p>
     * The simple interceptor rocket continually steers towards an incoming rocket to intercept it.
     */
    @Override
    protected void update() {

        if (targetRocket.isDestroyed()) {
            targetRocket = null;
            List<Rocket> rockets = world.getEntitiesByType(Rocket.class);
            for (Rocket rocket : rockets) {
                if (rocket.getSide() != side) {
                    this.targetRocket = rocket;
                }
            }
        }

        // Calculate passed in-world time
        final float currentTime = world.getCurrentTime();
        float deltaTime = currentTime - lastUpdateTime;
        lastUpdateTime = currentTime;

        // Copy position and velocity values
        Vector2f newPosition = new Vector2f(position);
        Vector2f newVelocity = new Vector2f(velocity);

        if (targetRocket == null || targetRocket.isDestroyed()) {
            setWillBeDestroyed(true);
            return;
        }
        // Calculate vector pointing to target
        Vector2f targetDirection = new Vector2f(targetRocket.getPosition().x - position.x, targetRocket.getPosition().y - position.y);

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
        final float noiseScale = 0.1f;

        turnAngle += (SimplexNoise.noise(position.x * noiseScale, position.y * noiseScale)) * 1 * errorStrength * deltaTime;

        // Rotate the velocity vector by turnAngle
        newVelocity = new Vector2f(
                (float) (Math.cos(turnAngle) * newVelocity.x - Math.sin(turnAngle) * newVelocity.y),
                (float) (Math.sin(turnAngle) * newVelocity.x + Math.cos(turnAngle) * newVelocity.y));

        // Save new Velocity
        velocity = newVelocity;

        newVelocity = new Vector2f(newVelocity);

        // Apply time scaling to calculated velocity and apply it
        newVelocity.scale(deltaTime, velocity);
        newPosition.add(newVelocity);

        // Save new position
        setPosition(newPosition);

        if (shouldExplode()) {
            List<Entity> entities = world.getEntitiesByPosition(position, 10);
            for (Entity entity : entities) {
                if (entity.getSide() != side) {
                    entity.setWillBeDestroyed(true);
                }
            }
            this.setWillBeDestroyed(true);
        }
    }

    /**
     * @return true if the target rocket is near or has been missed. False if otherwise.
     */
    @Override
    protected boolean shouldExplode() {
        if (targetRocket.isDestroyed()) {
            return false;
        }
        Vector2f difference = new Vector2f(targetRocket.getPosition().x - position.x, targetRocket.getPosition().y - position.y);
        return difference.length() < 5;
    }
}
