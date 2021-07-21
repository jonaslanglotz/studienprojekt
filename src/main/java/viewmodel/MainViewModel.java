package main.java.viewmodel;


import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import main.java.model.Vector2D;
import main.java.model.WorldModel;
import main.java.model.world.Base;
import main.java.model.world.Entity;
import main.java.model.world.Side;
import main.java.model.world.Util;
import main.java.model.world.rockets.Rocket;
import main.java.model.world.rockets.RocketType;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class MainViewModel {
    @Getter
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

    @Getter
    private BooleanProperty defenderAutomaticMode;

    @Getter
    private DoubleProperty defenderLaunchSpeed;

    @Getter
    private IntegerProperty defenderRocketsPerThreat;

    @Getter
    private ObjectProperty<Entity> selectedEntity;

    @Getter
    private BooleanProperty entityLock;

    @Getter
    private final DoubleProperty requestedSimulationSpeed = new SimpleDoubleProperty(1);

    @Getter
    private final DoubleProperty actualSimulationSpeed = new SimpleDoubleProperty(requestedSimulationSpeed.get());

    public MainViewModel(WorldModel worldModel) {
        this.worldModel = worldModel;

        entities = new SimpleListProperty<>(
                FXCollections.observableArrayList(this.worldModel.getEntities()));

        requestedSimulationSpeed.addListener((observableValue, number, t1) -> worldModel.setWantedSimulationSpeed(t1.doubleValue()));

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
                FXCollections.observableArrayList(Arrays.stream(RocketType.values()).map(Enum::name).collect(Collectors.toList()))
        );

        attackerStartSelection = new SimpleStringProperty(attackerStartSelectables.getValue().get(0));
        attackerTargetSelection = new SimpleStringProperty(attackerTargetSelectables.getValue().get(0));
        defenderStartSelection = new SimpleStringProperty(defenderStartSelectables.getValue().get(0));
        defenderRocketTypeSelection = new SimpleStringProperty(defenderRocketTypeSelectables.getValue().get(0));

        defenderRocketTypeSelection.addListener((observable, oldValue, newValue) -> {
            worldModel.getEntitiesByType(Base.class).forEach(base -> base.setDefenseRocketType(RocketType.valueOf(newValue)));
        });

        defenderAutomaticMode = new SimpleBooleanProperty();

        defenderAutomaticMode.addListener((observable, oldValue, newValue) -> {
            worldModel.getEntitiesByType(Base.class).forEach(base -> base.setInAutomaticMode(newValue));
        });

        defenderLaunchSpeed = new SimpleDoubleProperty(3);
        defenderLaunchSpeed.addListener((observable, oldValue, newValue) -> {
            worldModel.getEntitiesByType(Base.class).forEach(base -> base.setLaunchSpeed(newValue.doubleValue()));
        });

        defenderRocketsPerThreat = new SimpleIntegerProperty(3);
        defenderRocketsPerThreat.addListener((observable, oldValue, newValue) -> {
            System.out.println(newValue.intValue());
            worldModel.getEntitiesByType(Base.class).forEach(base -> base.setDefenseRocketsPerThreat(newValue.intValue()));
        });

        selectedEntity = new SimpleObjectProperty<>();
        entityLock = new SimpleBooleanProperty();

        this.worldModel.addPropertyChangeListener(this::updateValues);
    }

    public void updateValues(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case "entities":
                Util.batch(this.hashCode() + "entities", () -> Platform.runLater(() -> {
                    entities.setAll(worldModel.getEntities());

                    if (selectedEntity.get() != null && selectedEntity.get().isWillBeDestroyed()) {
                        selectedEntity.set(null);
                    }

                    if (selectedEntity.get() != null && !selectedEntity.get().isWillBeDestroyed() && entityLock.get()) {
                        centerWorldX.set(selectedEntity.get().getPosition().x);
                        centerWorldY.set(selectedEntity.get().getPosition().y);
                    }
                }), 60);
                break;

            case "simulationSpeed":
                Util.batch(this.hashCode() + "simulationSpeed", () -> Platform.runLater(() -> {
                    actualSimulationSpeed.set((Double) evt.getNewValue());
                }), 60);
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

    public void dragMap(double x, double y) {
        centerWorldX.set(centerWorldX.floatValue() + x / zoom.getValue());
        centerWorldY.set(centerWorldY.floatValue() + y / zoom.getValue());
        entityLock.set(false);
    }

    public void zoomMap(double v) {
        zoom.set(Math.max(0.1, zoom.getValue() + zoom.getValue() * v));
    }

    public void spawnDefenderRockets() {

        ArrayList<Rocket> threats = worldModel.getEntitiesByType(Rocket.class).stream()
                .filter(rocket -> rocket.getSide() == Side.ATTACKER && !rocket.isWillBeDestroyed()).collect(Collectors.toCollection(ArrayList::new));

        Base spawnBase = getBaseFromName(defenderStartSelection.getValue());

        threats.forEach(spawnBase::spawnDefendingRocket);
    }

    public void selectEntityAtCoordinates(Vector2D position, double radius) {
        selectedEntity.set(worldModel.getEntitiesByPosition(position, radius).stream().findFirst().orElse(null));
        entityLock.set(true);
    }
}
