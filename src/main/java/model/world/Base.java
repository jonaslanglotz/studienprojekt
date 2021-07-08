package main.java.model.world;

import lombok.Getter;
import main.java.model.Vector2D;
import main.java.model.WorldModel;
import main.java.model.world.rockets.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Base extends DynamicEntity {
    /**
     * @param world    Position of the entity in world coordinates.
     * @param position The world this entity exists in.
     */
    public Base(WorldModel world, Vector2D position, Side side) {
        super(world, position, side);
    }

    public String getName() {
        return "Basis#" + this.id;
    }

    public void spawnAttackingRocket(int updateInterval, double errorStrength, Base targetBase, double speed, double steerRate) {
        Rocket rocket = new Rocket(
                world,
                new Vector2D(position.x + (Math.random() - 0.5) * 20.0, position.y + (Math.random() - 0.5) * 20.0),
                side,
                updateInterval,
                errorStrength,
                targetBase.getPosition(),
                speed,
                steerRate
        );
        world.spawn(rocket);
    }

    public void spawnAttackingRockets(int updateInterval, double errorStrength, Base targetBase, double speed, double steerRate, int amount) {
        for (int i = 0; i < amount; i++) {
            spawnAttackingRocket(updateInterval, errorStrength, targetBase, speed, steerRate);
        }
    }

    public FlakRocket spawnDefendingFlakRocket(int updateInterval, Rocket target, double speed) {
        Vector2D startPosition = new Vector2D(position.x + (Math.random() - 0.5) * 20.0, position.y + (Math.random() - 0.5) * 20.0);
        Vector2D targetPosition = Util.calculateIntersectionCoordinates(target.getPosition(), target.getVelocity(), startPosition, speed);

        if (targetPosition == null) {
            return null;
        }

        FlakRocket flakRocket = new FlakRocket(
                world,
                startPosition,
                side,
                updateInterval,
                targetPosition,
                speed
        );

        world.spawn(flakRocket);
        return flakRocket;
    }


    public SimpleInterceptorRocket spawnDefendingSimpleInterceptorRocket(int updateInterval, double errorStrength, Rocket targetRocket, double speed, double steerRate) {
        SimpleInterceptorRocket rocket = new SimpleInterceptorRocket(
                world,
                new Vector2D(position.x + (Math.random() - 0.5) * 20.0, position.y + (Math.random() - 0.5) * 20.0),
                side,
                updateInterval,
                errorStrength,
                speed,
                steerRate,
                targetRocket
        );

        world.spawn(rocket);
        return rocket;
    }

    public AdvancedInterceptorRocket spawnDefendingAdvancedInterceptorRocket(int updateInterval, double errorStrength, Rocket targetRocket, double speed, double steerRate) {
        AdvancedInterceptorRocket rocket = new AdvancedInterceptorRocket(
                world,
                new Vector2D(position.x + (Math.random() - 0.5) * 20.0, position.y + (Math.random() - 0.5) * 20.0),
                side,
                updateInterval,
                errorStrength,
                speed,
                steerRate,
                targetRocket
        );

        world.spawn(rocket);
        return rocket;
    }

    public Rocket spawnDefendingRocket(Rocket threat) {
        switch (defenseRocketType) {
            case FLAK -> {
                return spawnDefendingFlakRocket(20, threat, 100);
            }
            case SIMPLE_INTERCEPTOR -> {
                return spawnDefendingSimpleInterceptorRocket(20, 2, threat, 100, 1);
            }
            case ADVANCED_INTERCEPTOR -> {
                return spawnDefendingAdvancedInterceptorRocket(20, 2, threat, 100, 1);
            }
            default -> throw new IllegalStateException("Unexpected value: " + defenseRocketType);
        }

    }

    @Getter
    boolean inAutomaticMode = false;

    public void setInAutomaticMode(boolean value) {
        boolean oldValue = inAutomaticMode;
        inAutomaticMode = value;
        changes.firePropertyChange("inAutomaticMode", oldValue, value);
    }

    @Getter
    double launchSpeed = 3;

    public void setLaunchSpeed(double value) {
        double oldValue = launchSpeed;
        launchSpeed = value;
        changes.firePropertyChange("launchSpeed", oldValue, value);
    }

    @Getter
    int defenseRocketsPerThreat = 3;

    public void setDefenseRocketsPerThreat(int value) {
        double oldValue = defenseRocketsPerThreat;
        defenseRocketsPerThreat = value;
        changes.firePropertyChange("defenseRocketsPerThreat", oldValue, value);
    }

    @Getter
    RocketType defenseRocketType = RocketType.FLAK;

    public void setDefenseRocketType(RocketType value) {
        RocketType oldValue = defenseRocketType;
        defenseRocketType = value;
        changes.firePropertyChange("defenseRocketType", oldValue, value);
    }

    HashMap<Rocket, List<Rocket>> threatMap = new HashMap<>();
    double lastLaunch = 0;

    @Override
    protected void update(double deltaTime) {
        double now = world.getCurrentTime();

        if (!inAutomaticMode) {
            lastLaunch = now;
            return;
        }

        List<Rocket> threats = world.getEntitiesByPosition(position, 250)
                .stream()
                .filter(entity -> entity.getClass().equals(Rocket.class))
                .map(entity -> (Rocket) entity)
                .filter(rocket -> rocket.side != side).collect(Collectors.toList());

        // Purge rockets that are no longer relevant
        ArrayList<Rocket> threatsToBeRemoved = new ArrayList<>();
        threatMap.forEach((rocket, rockets) -> {
            if (!threats.contains(rocket)) {
                threatsToBeRemoved.add(rocket);
            }

            // Purge destroyed defensive rockets
            ArrayList<Rocket> firedRocketsToBeRemoved = new ArrayList<>();
            rockets.forEach(firedRocket -> {
                if (firedRocket.isDestroyed()) {
                    firedRocketsToBeRemoved.add(firedRocket);
                }
            });
            rockets.removeAll(firedRocketsToBeRemoved);
        });
        threatsToBeRemoved.forEach(rocket -> threatMap.remove(rocket));

        // Add new threats
        threats.forEach(rocket -> {
            if (!threatMap.containsKey(rocket)) {
                threatMap.put(rocket, new ArrayList<>());
            }
        });

        if (threats.size() == 0) {
            lastLaunch = now;
        }

        int rocketsToFire = (int) ((now - lastLaunch) / (1.0 / launchSpeed));
        outer:
        for (int i = 0; i < rocketsToFire; i++) {
            for (Rocket key :
                    threats) {
                if (threatMap.get(key).size() < defenseRocketsPerThreat) {
                    Rocket added = spawnDefendingRocket(key);
                    if (added != null) {
                        threatMap.get(key).add(added);
                        lastLaunch = now;
                    }
                    continue outer;
                }
            }
        }


    }
}
