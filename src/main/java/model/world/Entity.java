package main.java.model.world;

import main.java.model.WorldModel;

import javax.vecmath.Vector2f;


/**
 * Base class for all entities contained in a world
 */
public class Entity {
    private Vector2f position;
    public final int creationTime;
    private WorldModel world;

    /**
     * @param world The world this entity is spawned in
     * @param position The position in the world the entity is spawned at
     */
    public Entity(WorldModel world, Vector2f position) {
        this.world = world;
        this.position = position;
        this.creationTime = this.world.getCurrentTime();
    }

    public Vector2f getPosition() {
        return this.position;
    }

    public Vector2f setPosition() {
        return this.position;
    }
}
