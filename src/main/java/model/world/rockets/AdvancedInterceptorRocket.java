package main.java.model.world.rockets;

import lombok.NonNull;
import main.java.model.Vector2D;
import main.java.model.WorldModel;
import main.java.model.world.Entity;
import main.java.model.world.Side;
import main.java.model.world.Util;

import java.util.List;
import java.util.stream.Collectors;

public class AdvancedInterceptorRocket extends Rocket {

    private Rocket targetRocket;

    /**
     * @param world          The world this entity is spawned in
     * @param position       The position in the world the entity is spawned at
     * @param updateInterval
     * @param errorRate
     * @param velocity
     * @param steerRate
     */
    public AdvancedInterceptorRocket(WorldModel world, Vector2D position, Side side, int updateInterval, double errorRate, Vector2D velocity, double steerRate, Rocket targetRocket) {
        super(world, position, side, updateInterval, errorRate, targetRocket.getPosition(), velocity, steerRate);
        this.targetRocket = targetRocket;
    }

    public AdvancedInterceptorRocket(WorldModel world, Vector2D position, Side side, int updateInterval, double errorRate, double speed, double steerRate, @NonNull Rocket targetRocket) {
        super(world, position, side, updateInterval, errorRate, targetRocket.getPosition(), speed, steerRate);
        this.targetRocket = targetRocket;
    }

    /**
     * Perform one simulation step for this rocket.
     * <p>
     * The advanced interceptor rocket tries to predict the future path of an incoming rocket, and intercepting a predicted future point in space.
     *
     * @param deltaTime
     */
    @Override
    protected void update(double deltaTime) {
        if (targetRocket == null || targetRocket.isWillBeDestroyed()) {
            targetRocket = null;
            List<Rocket> rockets = world.getEntitiesByType(Rocket.class);
            for (Rocket rocket : rockets) {
                if (rocket.getSide() != side && !rocket.isDestroyed()) {
                    Vector2D intersection = Util.calculateIntersectionCoordinates(rocket.getPosition(), rocket.getVelocity(), position, velocity.length());
                    if (intersection != null) {
                        targetPosition = intersection;
                        this.targetRocket = rocket;
                    }
                }
            }
        } else {
            Vector2D intersection = Util.calculateIntersectionCoordinates(targetRocket.getPosition(), targetRocket.getVelocity(), position, velocity.length());
            if (intersection == null) {
                targetRocket = null;
            } else {
                targetPosition = intersection;
            }
        }

        super.update(deltaTime);

        if (shouldExplode()) {
            List<Entity> entities = world.getEntitiesByPosition(position, 10).stream().filter(entity -> entity instanceof Rocket).collect(Collectors.toList());
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
        if (targetRocket == null) {
            return Math.random() < (world.getUpdateInterval() + 1) * 0.001;
        }

        Vector2D difference = targetPosition.sub(position);
        return difference.length() < 5;
    }
}
