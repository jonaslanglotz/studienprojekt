package main.java.model.world.rockets;

import main.java.model.Vector2D;
import main.java.model.WorldModel;
import main.java.model.world.Entity;
import main.java.model.world.Side;

import java.util.List;
import java.util.stream.Collectors;

public class FlakRocket extends Rocket {

    /**
     * @param world          The world this entity is spawned in
     * @param position       The position in the world the entity is spawned at
     * @param updateInterval
     * @param targetPosition
     * @param velocity
     */
    public FlakRocket(WorldModel world, Vector2D position, Side side, int updateInterval,
                      Vector2D targetPosition, Vector2D velocity) {
        super(world, position, side, updateInterval, 0, targetPosition, velocity, 0);
    }

    public FlakRocket(WorldModel world, Vector2D position, Side side, int updateInterval,
                      Vector2D targetPosition, double speed) {
        super(world, position, side, updateInterval, 0, targetPosition, speed, 0);
    }

    @Override
    protected void update(double deltaTime) {
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

    double lastDistance = Double.POSITIVE_INFINITY;

    /**
     * @return true if the rocket has reached the target position or is ahead of it. False if it has not yet done so.
     */
    @Override
    protected boolean shouldExplode() {
        double distance = targetPosition.sub(position).length();

        if (distance > lastDistance) {
            return true;
        }

        lastDistance = distance;
        return distance < 2;
    }
}
