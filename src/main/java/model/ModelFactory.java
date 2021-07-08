package main.java.model;

import lombok.Getter;
import main.java.model.world.Base;
import main.java.model.world.Side;

public class ModelFactory {

    @Getter
    WorldModel worldModel;

    public ModelFactory() {
        worldModel = new DefaultWorldModel(1000, 1000, 1, 20);
        for (int i = 0; i < 3; i++) {
            worldModel.spawn(new Base(worldModel, generateBaseCoordinates(0, 20, 700, 200), Side.ATTACKER));
        }
        for (int i = 0; i < 3; i++) {
            worldModel.spawn(new Base(worldModel, generateBaseCoordinates(0, 500, 700, 680), Side.DEFENDER));
        }
    }

    private Vector2D generateBaseCoordinates(float minX, float minY, float maxX, float maxY) {
        double x = minX + Math.random() * (maxX - minX);
        double y = minY + Math.random() * (maxY - minY);
        Vector2D position = new Vector2D(x, y);
        for (Base base :
                worldModel.getEntitiesByType(Base.class)) {
            Vector2D basePosition = base.getPosition();
            Vector2D difference = basePosition.sub(position);
            if (difference.length() <= 10) {
                return generateBaseCoordinates(minX, minY, maxX, maxY);
            }
        }
        return position;
    }
}
