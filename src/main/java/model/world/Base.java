package main.java.model.world;

import main.java.model.WorldModel;

import javax.vecmath.Vector2f;

public class Base extends Entity {

    /**
     * @param world    The world this entity is spawned in
     * @param position The position in the world the entity is spawned at
     */
    public Base(WorldModel world, Vector2f position) {
        super(world, position);
    }
}
