package main.java.viewmodel;


import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import main.java.model.WorldModel;
import main.java.model.world.Base;
import main.java.model.world.Entity;
import main.java.model.world.Side;
import main.java.model.world.rockets.Rocket;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

public class MainViewModel {
    WorldModel worldModel;

    private ListProperty<Entity> entities;
    @Getter
    private DoubleProperty centerWorldX;
    @Getter
    private DoubleProperty centerWorldY;
    @Getter
    private DoubleProperty zoom;
    @Getter
    private DoubleProperty worldWidth;
    @Getter
    private DoubleProperty worldHeight;

    @Getter
    private DoubleProperty attackerSpeed;

    @Getter
    private DoubleProperty attackerErrorStrength;

    @Getter
    private DoubleProperty attackerRocketAmount;

    @Getter
    private ObjectProperty<ObservableList<String>> attackerStartSelectables;

    @Getter
    private ObjectProperty<ObservableList<String>> attackerTargetSelectables;

    @Getter
    private StringProperty attackerStartSelection;

    @Getter
    private StringProperty attackerTargetSelection;

    @Getter
    private ObjectProperty<ObservableList<String>> defenderStartSelectables;

    @Getter
    private StringProperty defenderStartSelection;

    @Getter
    private ObjectProperty<ObservableList<String>> defenderRocketTypeSelectables;

    @Getter
    private StringProperty defenderRocketTypeSelection;


    public MainViewModel(WorldModel worldModel) {
        this.worldModel = worldModel;

        entities = new SimpleListProperty<>(
                FXCollections.observableArrayList(this.worldModel.getEntities()));


        centerWorldX = new SimpleDoubleProperty(worldModel.getWidth() / 2);
        centerWorldY = new SimpleDoubleProperty(worldModel.getHeight() / 2);
        zoom = new SimpleDoubleProperty(1);
        worldWidth = new SimpleDoubleProperty(worldModel.getWidth());
        worldHeight = new SimpleDoubleProperty(worldModel.getHeight());

        attackerSpeed = new SimpleDoubleProperty(50);
        attackerErrorStrength = new SimpleDoubleProperty(2);
        attackerRocketAmount = new SimpleDoubleProperty(1);

        ArrayList<Base> bases = new ArrayList<>(worldModel.getEntitiesByType(Base.class));
        ArrayList<Base> attackerBases = (ArrayList<Base>) bases.stream().filter(base -> base.getSide() == Side.ATTACKER).collect(Collectors.toList());
        ArrayList<Base> defenderBases = (ArrayList<Base>) bases.stream().filter(base -> base.getSide() == Side.DEFENDER).collect(Collectors.toList());


        attackerStartSelectables = new SimpleObjectProperty<>(
                FXCollections.observableArrayList(getBaseNames(attackerBases)));

        attackerTargetSelectables = new SimpleObjectProperty<>(
                FXCollections.observableArrayList(getBaseNames(defenderBases)));

        defenderStartSelectables = new SimpleObjectProperty<>(
                FXCollections.observableArrayList(getBaseNames(defenderBases)));

        defenderRocketTypeSelectables = new SimpleObjectProperty<>(
                FXCollections.observableArrayList("Flak", "SimpleInterceptor", "AdvancedInterceptor")
        );

        attackerStartSelection = new SimpleStringProperty(attackerStartSelectables.getValue().get(0));
        attackerTargetSelection = new SimpleStringProperty(attackerTargetSelectables.getValue().get(0));
        defenderStartSelection = new SimpleStringProperty(defenderStartSelectables.getValue().get(0));
        defenderRocketTypeSelection = new SimpleStringProperty(defenderRocketTypeSelectables.getValue().get(0));

        this.worldModel.addPropertyChangeListener(this::updateValues);
    }

    int updatesPerSecond = 60;
    long lastEntityUpdate = 0;
    Timer updateTimer = new Timer(true);
    boolean isTimerSet = false;

    public void updateValues(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case "entities":
                long now = System.nanoTime();
                long timeSinceLastUpdate = now - lastEntityUpdate;

                if (timeSinceLastUpdate < 1000000000f / updatesPerSecond) {
                    if (!isTimerSet) {
                        isTimerSet = true;
                        updateTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Platform.runLater(() -> {
                                    entities.setAll(worldModel.getEntities());
                                    lastEntityUpdate = System.nanoTime();
                                    isTimerSet = false;
                                });
                            }
                        }, (long) ((1000f / updatesPerSecond) - timeSinceLastUpdate / 1000000f));
                    }
                    return;
                }

                Platform.runLater(() -> entities.setAll((List<Entity>) evt.getNewValue()));
                lastEntityUpdate = System.nanoTime();

                break;

            default:
                break;
        }
    }


    private ArrayList<String> getBaseNames(ArrayList<Base> bases) {
        return (ArrayList<String>) bases.stream().map(Base::getName).collect(Collectors.toList());
    }

    public ListProperty<Entity> getEntities() {
        return entities;
    }

    public void spawnAttackerRockets() {
        getBaseFromName(attackerStartSelection.getValue()).spawnAttackingRockets(
                20,
                attackerErrorStrength.floatValue(),
                getBaseFromName(attackerTargetSelection.getValue()),
                attackerSpeed.floatValue(),
                1,
                attackerRocketAmount.intValue()
        );
    }

    public Base getBaseFromName(String name) {
        return worldModel.getEntitiesByType(Base.class).stream().filter(base -> base.getName().equals(name)).findFirst().orElse(null);
    }

    public void dragMap(float x, float y) {
        centerWorldX.set(centerWorldX.floatValue() + x / zoom.getValue());
        centerWorldY.set(centerWorldY.floatValue() + y / zoom.getValue());
    }

    public void zoomMap(double v) {
        zoom.set(Math.max(0.1, zoom.getValue() + zoom.getValue() * v));
    }

    public void spawnDefenderRockets() {
        switch (defenderRocketTypeSelection.getValue()) {
            case "Flak":
                worldModel.getEntitiesByType(Rocket.class).stream()
                        .filter(rocket -> rocket.getSide() == Side.ATTACKER)
                        .forEach(rocket -> {
                            getBaseFromName(defenderStartSelection.getValue())
                                    .spawnDefendingFlakRocket(
                                            20,
                                            rocket,
                                            100
                                    );
                        });

                break;
            case "SimpleInterceptor":
                worldModel.getEntitiesByType(Rocket.class).stream()
                        .filter(rocket -> rocket.getSide() == Side.ATTACKER)
                        .forEach(rocket -> {
                            getBaseFromName(defenderStartSelection.getValue())
                                    .spawnDefendingSimpleInterceptorRocket(
                                            20,
                                            2,
                                            rocket,
                                            100,
                                            1
                                    );
                        });

                break;

            case "AdvancedInterceptor":
                worldModel.getEntitiesByType(Rocket.class).stream()
                        .filter(rocket -> rocket.getSide() == Side.ATTACKER)
                        .forEach(rocket -> {
                            getBaseFromName(defenderStartSelection.getValue())
                                    .spawnDefendingAdvancedInterceptorRocket(
                                            20,
                                            2,
                                            rocket,
                                            100,
                                            1
                                    );
                        });

                break;
            default:
                break;
        }
    }
}
