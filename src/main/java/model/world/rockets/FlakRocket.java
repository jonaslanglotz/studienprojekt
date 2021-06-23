package main.java.model.world.rockets;

import main.java.model.WorldModel;
import main.java.model.world.Entity;
import main.java.model.world.Side;

import javax.vecmath.Vector2f;
import java.util.List;

public class FlakRocket extends Rocket {

    /**
     * @param world          The world this entity is spawned in
     * @param position       The position in the world the entity is spawned at
     * @param updateInterval
     * @param targetPosition
     * @param velocity
     */
    public FlakRocket(WorldModel world, Vector2f position, Side side, int updateInterval,
                      Vector2f targetPosition, Vector2f velocity) {
        super(world, position, side, updateInterval, 0, targetPosition, velocity, 0);
    }

    public FlakRocket(WorldModel world, Vector2f position, Side side, int updateInterval,
                      Vector2f targetPosition, float speed) {
        super(world, position, side, updateInterval, 0, targetPosition, speed, 0);
    }

    @Override
    protected void update() {
        // Calculate passed in-world time
        final float currentTime = world.getCurrentTime();
        float deltaTime = currentTime - lastUpdateTime;
        lastUpdateTime = currentTime;

        // Copy position and velocity values
        Vector2f newPosition = new Vector2f(position);
        Vector2f velocityCopy = new Vector2f(velocity);

        // Apply time scaling to calculated velocity and apply it
        velocityCopy.scale(deltaTime);
        newPosition.add(velocityCopy);

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
     * @return true if the rocket has reached the target position or is ahead of it. False if it has not yet done so.
     */
    @Override
    protected boolean shouldExplode() {
        Vector2f difference = new Vector2f(targetPosition.x - position.x, targetPosition.y - position.y);
        return difference.length() < 5;
    }
}
