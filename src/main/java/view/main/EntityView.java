package main.java.view.main;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import lombok.Getter;
import main.java.model.world.Base;
import main.java.model.world.Entity;
import main.java.model.world.Util;
import main.java.model.world.rockets.AdvancedInterceptorRocket;
import main.java.model.world.rockets.FlakRocket;
import main.java.model.world.rockets.Rocket;
import main.java.model.world.rockets.SimpleInterceptorRocket;

import java.beans.PropertyChangeListener;
import java.io.IOException;

abstract class CustomEvent extends Event {
    public static final EventType<CustomEvent> CUSTOM_EVENT_TYPE = new EventType<>(ANY);

    public CustomEvent(EventType<? extends Event> eventType) {
        super(eventType);
    }

    public abstract void invokeHandler(CustomEventHandler handler);
}

abstract class CustomEventHandler implements EventHandler<CustomEvent> {
    public abstract void onEntityLinkClickedEvent(Entity entity);

    @Override
    public void handle(CustomEvent event) {
        event.invokeHandler(this);
    }
}

class EntityLinkClickedEvent extends CustomEvent {
    public static final EventType<CustomEvent> ENTITY_LINK_CLICKED_EVENT_TYPE = new EventType(CUSTOM_EVENT_TYPE, "EntityLinkClickedEvent");

    private final Entity entity;

    public EntityLinkClickedEvent(Entity entity) {
        super(ENTITY_LINK_CLICKED_EVENT_TYPE);
        this.entity = entity;
    }

    @Override
    public void invokeHandler(CustomEventHandler handler) {
        handler.onEntityLinkClickedEvent(entity);
    }
}

public class EntityView extends VBox {
    @Getter
    ObjectProperty<Entity> entity = new SimpleObjectProperty<>();

    @Getter
    BooleanProperty isViewLocked = new SimpleBooleanProperty();

    @FXML
    Text header;

    @FXML
    Text startPosition;

    @FXML
    Text startTime;

    @FXML
    Hyperlink target;

    @FXML
    Text targetCoordinates;

    @FXML
    Text status;

    @FXML
    Text estimate;

    @FXML
    StackPane overlayStackPane;

    @FXML
    VBox mainContainer;

    @FXML
    BorderPane placeholderPane;

    @FXML
    ToggleButton lockViewToggle;

    PropertyChangeListener listener = evt -> Util.batch(String.valueOf(this.hashCode()), () -> Platform.runLater(this::update), 60);
    ;

    public EntityView() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/EntityView.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception){
            throw new RuntimeException(exception);
        }

        overlayStackPane.prefHeightProperty().bind(heightProperty());
        overlayStackPane.prefWidthProperty().bind(widthProperty());

        entity.addListener((observable, oldValue, newValue) -> {
            System.out.println("new");
            if (oldValue != null) {
                oldValue.removePropertyChangeListener(listener);
            }
            if (newValue != null) {
                newValue.addPropertyChangeListener(listener);
            }
            boolean entitySelected = newValue != null;
            mainContainer.setVisible(entitySelected);
            placeholderPane.setVisible(!entitySelected);
            Util.batch(String.valueOf(this.hashCode()), () -> Platform.runLater(this::update), 60);
        });

        isViewLocked.bindBidirectional(lockViewToggle.selectedProperty());

    }

    static private String entityName(Entity entity) {
        String typename = "Unknown";

        if (entity.getClass().equals(Rocket.class)) {
            typename = "Rocket";
        }

        if (entity.getClass().equals(FlakRocket.class)) {
            typename = "FlakRocket";
        }

        if (entity.getClass().equals(SimpleInterceptorRocket.class)) {
            typename = "SimpleInterceptorRocket";
        }

        if (entity.getClass().equals(AdvancedInterceptorRocket.class)) {
            typename = "AdvancedInterceptorRocket";
        }

        if (entity.getClass().equals(Base.class)) {
            typename = "Base";
        }

        return String.format("%s#%d", typename, entity.getId());

    }

    private void update() {
        Entity entity = this.entity.get();

        if (entity == null) {
            return;
        }

        header.setText("  " + entityName(entity));

        // Startposition
        if (entity.getClass().equals(Base.class)) {
            startPosition.setText(entity.getPosition().toString());
        } else {
            startPosition.setText(((Rocket) entity).getStartPosition().toString());
        }

        // Startzeit
        startTime.setText(String.format("%.2f min", entity.getCreationTime()));

        // Ziel
        if (entity.getClass().equals(SimpleInterceptorRocket.class) || entity.getClass().equals(AdvancedInterceptorRocket.class)) {
            Entity targetEntity;
            if (entity.getClass().equals(SimpleInterceptorRocket.class)) {
                targetEntity = ((SimpleInterceptorRocket) entity).getTargetRocket();
            } else {
                targetEntity = ((AdvancedInterceptorRocket) entity).getTargetRocket();
            }

            target.setText(entityName(targetEntity));
            target.setOnMouseClicked(event -> {
                this.fireEvent(new EntityLinkClickedEvent(targetEntity));
            });
        } else {
            target.setText("None");
            target.setOnMouseClicked(null);
        }

        // Zielkoordinate
        if (entity.getClass().equals(Base.class)) {
            targetCoordinates.setText("None");
        } else {
            targetCoordinates.setText(((Rocket) entity).getTargetPosition().toString());
        }
        // Status
        status.setText(entity.isWillBeDestroyed() ? "Zerst\u00F6rt" : "Aktiv");

        // Estimate
        if (entity.getClass().equals(Base.class)) {
            estimate.setText("None");
        } else {
            estimate.setText(String.format("%.2f min", ((Rocket) entity).estimatedTimeToTarget()));
        }

    }

}
