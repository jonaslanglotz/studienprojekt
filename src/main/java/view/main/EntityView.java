package main.java.view.main;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import lombok.Getter;
import main.java.model.world.Entity;
import main.java.model.world.Util;

import java.beans.PropertyChangeListener;
import java.io.IOException;

public class EntityView extends VBox {
    @Getter
    ObjectProperty<Entity> entity = new SimpleObjectProperty<>();

    @FXML
    Text header;

    @FXML
    StackPane overlayStackPane;

    @FXML
    VBox mainContainer;

    @FXML
    BorderPane placeholderPane;

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


    }

    private void update() {
        Entity entity = this.entity.get();

        if (entity == null) {
            return;
        }

        header.setText(String.valueOf(System.currentTimeMillis()));
    }

}
