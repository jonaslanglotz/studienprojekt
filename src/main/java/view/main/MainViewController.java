package main.java.view.main;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import main.java.viewmodel.MainViewModel;

import javax.vecmath.Vector2f;

public class MainViewController {
    MainViewModel mainViewModel;

    @FXML
    MapView mapView;


    /* Defender Options */

    @FXML
    ComboBox<String> defenderRocketTypeComboBox;

    @FXML
    ComboBox<String> defenderStartComboBox;

    @FXML
    Button defenderSpawnButton;


    /* Attacker Options */

    @FXML
    Slider attackerSpeedSlider;

    @FXML
    Slider attackerErrorStrengthSlider;

    @FXML
    Slider attackerRocketAmountSlider;

    @FXML
    ComboBox<String> attackerStartComboBox;

    @FXML
    ComboBox<String> attackerTargetComboBox;

    @FXML
    Button attackerSpawnButton;

    Vector2f lastMouseCoordinates;

    public void init(MainViewModel mainViewModel) {
        this.mainViewModel = mainViewModel;
        mapView.getEntities().bind(mainViewModel.getEntities());
        mapView.getCenterWorldX().bind(mainViewModel.getCenterWorldX());
        mapView.getCenterWorldY().bind(mainViewModel.getCenterWorldY());
        mapView.getZoom().bind(mainViewModel.getZoom());
        mapView.getWorldWidth().bind(mainViewModel.getWorldWidth());
        mapView.getWorldHeight().bind(mainViewModel.getWorldHeight());

        attackerSpeedSlider.valueProperty().bindBidirectional(mainViewModel.getAttackerSpeed());
        attackerErrorStrengthSlider.valueProperty().bindBidirectional(mainViewModel.getAttackerErrorStrength());
        attackerRocketAmountSlider.valueProperty().bindBidirectional(mainViewModel.getAttackerRocketAmount());

        attackerStartComboBox.itemsProperty().bindBidirectional(mainViewModel.getAttackerStartSelectables());
        attackerTargetComboBox.itemsProperty().bindBidirectional(mainViewModel.getAttackerTargetSelectables());
        defenderStartComboBox.itemsProperty().bindBidirectional(mainViewModel.getDefenderStartSelectables());

        attackerStartComboBox.valueProperty().bindBidirectional(mainViewModel.getAttackerStartSelection());
        attackerTargetComboBox.valueProperty().bindBidirectional(mainViewModel.getAttackerTargetSelection());
        defenderStartComboBox.valueProperty().bindBidirectional(mainViewModel.getDefenderStartSelection());

    }

    public void onAttackerSpawnButton(ActionEvent actionEvent) {
        mainViewModel.spawnAttackerRockets();
    }

    public void onDefenderSpawnButton(ActionEvent actionEvent) {
        //mainViewModel.spawnEntity();
    }

    public void onMapViewDragged(MouseEvent mouseEvent) {
        Vector2f coordinates = new Vector2f((float) mouseEvent.getX(), (float) mouseEvent.getY());
        Vector2f delta = new Vector2f(coordinates);
        delta.sub(lastMouseCoordinates);
        lastMouseCoordinates = coordinates;
        mainViewModel.dragMap(-delta.x, -delta.y);
    }

    public void onMapViewPressed(MouseEvent mouseEvent) {
        lastMouseCoordinates = new Vector2f((float) mouseEvent.getX(), (float) mouseEvent.getY());
    }

    public void onMapViewScroll(ScrollEvent scrollEvent) {
        mainViewModel.zoomMap(scrollEvent.getDeltaY() / 400);
    }
}
