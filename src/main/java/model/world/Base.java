package main.java.model.world;

import lombok.Getter;
import lombok.Setter;
import main.java.model.WorldModel;

import javax.vecmath.Vector2f;

public class Base extends Entity {


    @Getter
    @Setter
    private Side side;

    /**
     * @param world    Position of the entity in world coordinates.
     * @param position The world this entity exists in.
     */
    public Base(WorldModel world, Vector2f position, Side side) {
        super(world, position);
        this.side = side;
    }

    public String getName() {
        return "Basis#" + this.id;
    }
}
