package main.java.model;

import lombok.Getter;
import main.java.model.world.Base;
import main.java.model.world.Side;

import javax.vecmath.Vector2f;

public class ModelFactory {

    @Getter
    WorldModel worldModel;

    public ModelFactory() {
        worldModel = new DefaultWorldModel(1000, 1000, 1);
        for (int i = 0; i < 3; i++) {
            worldModel.spawn(new Base(worldModel, generateBaseCoordinates(0, 20, 700, 200), Side.ATTACKER));
        }
        for (int i = 0; i < 3; i++) {
            worldModel.spawn(new Base(worldModel, generateBaseCoordinates(0, 500, 700, 680), Side.DEFENDER));
        }
    }

    private Vector2f generateBaseCoordinates(float minX, float minY, float maxX, float maxY) {
        float x = (float) (minX + Math.random() * (maxX - minX));
        float y = (float) (minY + Math.random() * (maxY - minY));
        Vector2f position = new Vector2f(x, y);
        for (Base base :
                worldModel.getEntitiesByType(Base.class)) {
            Vector2f basePosition = base.getPosition();
            Vector2f difference = new Vector2f(basePosition.x - position.x, basePosition.y - position.y);
            if (difference.length() <= 10) {
                return generateBaseCoordinates(minX, minY, maxX, maxY);
            }
        }
        return position;
    }
}
