package main.java.model.world;

import main.java.model.WorldModel;

import javax.vecmath.Vector2f;

public class Base extends Entity {

    /**
     * @param world    Position of the entity in world coordinates.
     * @param position The world this entity exists in.
     */
    public Base(WorldModel world, Vector2f position) {
        super(world, position);
    }
}
