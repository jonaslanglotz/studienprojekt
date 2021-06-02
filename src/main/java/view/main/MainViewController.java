package main.java.view.main;

import javafx.beans.property.ListProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import main.java.model.world.Entity;
import main.java.viewmodel.MainViewModel;

public class MainViewController {
    MainViewModel mainViewModel;

    @FXML
    MapView mainCanvas;

    ListProperty<Entity> entities;

    @FXML
    StackPane stackPane;

    @FXML
    Button spawnButton;

    public void init(MainViewModel mainViewModel) {
        this.mainViewModel = mainViewModel;

        entities = mainCanvas.getEntities();
        entities.bind(mainViewModel.getEntities());
    }

    public void onSpawnButton(ActionEvent actionEvent) {
        mainViewModel.spawnEntity();
    }
}
