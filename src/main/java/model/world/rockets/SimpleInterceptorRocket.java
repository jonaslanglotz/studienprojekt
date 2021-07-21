package main.java.model.world.rockets;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import main.java.model.Vector2D;
import main.java.model.WorldModel;
import main.java.model.world.Entity;
import main.java.model.world.Side;

import java.util.List;
import java.util.stream.Collectors;

public class SimpleInterceptorRocket extends Rocket {

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
    public SimpleInterceptorRocket(WorldModel world, Vector2D position, Side side, int updateInterval, double errorRate, Vector2D velocity, double steerRate, @NonNull Rocket targetRocket) {
        super(world, position, side, updateInterval, errorRate, targetRocket.getPosition(), velocity, steerRate);
        this.targetRocket = targetRocket;
    }

    public SimpleInterceptorRocket(WorldModel world, Vector2D position, Side side, int updateInterval, double errorRate, double speed, double steerRate, @NonNull Rocket targetRocket) {
        super(world, position, side, updateInterval, errorRate, targetRocket.getPosition(), speed, steerRate);
        this.targetRocket = targetRocket;
    }

    /**
     * Perform one simulation step for this rocket.
     * <p>
     * The simple interceptor rocket continually steers towards an incoming rocket to intercept it.
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
                    this.targetRocket = rocket;
                    targetPosition = targetRocket.getPosition();
                }
            }
        } else {
            targetPosition = targetRocket.getPosition();
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
       
        Vector2D difference = targetRocket.getPosition().sub(position);
        return difference.length() < 5;
    }
}
