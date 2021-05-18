package main.java.model.world;

import main.java.model.WorldModel;

import javax.vecmath.Vector2f;

public class Tracer extends DynamicEntity {
    /**
     * @param world    The world this entity is spawned in
     * @param position The position in the world the entity is spawned at
     */
    public Tracer(WorldModel world, Vector2f position, int updateInterval) {
        super(world, position, updateInterval);
    }

    @Override
    protected void update() {
        // TODO Add logic
    }
}
