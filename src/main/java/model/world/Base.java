package main.java.model.world;

import lombok.Getter;
import lombok.Setter;
import main.java.model.WorldModel;
import main.java.model.world.rockets.AdvancedInterceptorRocket;
import main.java.model.world.rockets.FlakRocket;
import main.java.model.world.rockets.Rocket;
import main.java.model.world.rockets.SimpleInterceptorRocket;

import javax.vecmath.Vector2f;

public class Base extends DynamicEntity {
    /**
     * @param world    Position of the entity in world coordinates.
     * @param position The world this entity exists in.
     */
    public Base(WorldModel world, Vector2f position, Side side) {
        super(world, position, side, 50);
    }

    public String getName() {
        return "Basis#" + this.id;
    }

    public void spawnAttackingRocket(int updateInterval, float errorStrength, Base targetBase, float speed, float steerRate) {
        Rocket rocket = new Rocket(
                world,
                new Vector2f(position.x + ((float) Math.random() - 0.5f) * 20f, position.y + ((float) Math.random() - 0.5f) * 20f),
                side,
                updateInterval,
                errorStrength,
                targetBase.getPosition(),
                speed,
                steerRate
        );
        world.spawn(rocket);
    }

    public void spawnAttackingRockets(int updateInterval, float errorStrength, Base targetBase, float speed, float steerRate, int amount) {
        for (int i = 0; i < amount; i++) {
            spawnAttackingRocket(updateInterval, errorStrength, targetBase, speed, steerRate);
        }
    }

    public void spawnDefendingFlakRocket(int updateInterval, Rocket target, float speed) {
        Vector2f startPosition = new Vector2f(position.x + ((float) Math.random() - 0.5f) * 20f, position.y + ((float) Math.random() - 0.5f) * 20f);

        FlakRocket flakRocket = new FlakRocket(
                world,
                startPosition,
                side,
                updateInterval,
                Util.calculateIntersectionCoordinates(target.getPosition(), target.getVelocity(), startPosition, speed),
                speed
        );

        world.spawn(flakRocket);
    }


    public void spawnDefendingSimpleInterceptorRocket(int updateInterval, float errorStrength, Rocket targetRocket, float speed, float steerRate) {
        Rocket rocket = new SimpleInterceptorRocket(
                world,
                new Vector2f(position.x + ((float) Math.random() - 0.5f) * 20f, position.y + ((float) Math.random() - 0.5f) * 20f),
                side,
                updateInterval,
                errorStrength,
                speed,
                steerRate,
                targetRocket
        );

        world.spawn(rocket);
    }

    public void spawnDefendingAdvancedInterceptorRocket(int updateInterval, float errorStrength, Rocket targetRocket, float speed, float steerRate) {
        Rocket rocket = new AdvancedInterceptorRocket(
                world,
                new Vector2f(position.x + ((float) Math.random() - 0.5f) * 20f, position.y + ((float) Math.random() - 0.5f) * 20f),
                side,
                updateInterval,
                errorStrength,
                speed,
                steerRate,
                targetRocket
        );

        world.spawn(rocket);
    }

    @Getter
    @Setter
    boolean inAutomaticMode = true;

    @Override
    protected void update() {
        if (!inAutomaticMode) {
            return;
        }

        world.getEntitiesByPosition(position, 250)
                .stream()
                .filter(entity -> entity.getClass().equals(Rocket.class))
                .map(entity -> (Rocket) entity)
                .filter(rocket -> rocket.side != side)
                .forEach(threat -> {
                    if (threat.willBeDestroyed) {
                        return;
                    }
                    spawnDefendingFlakRocket(20, threat, 100);
                });

    }
}
