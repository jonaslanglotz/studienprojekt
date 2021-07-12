package main.java.view.main;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import main.java.model.Vector2D;
import main.java.model.world.Util;
import main.java.viewmodel.MainViewModel;

public class MainViewController {
    MainViewModel mainViewModel;

    @FXML
    MapView mapView;

    @FXML
    EntityView entityView;

    @FXML
    StackPane stackPane;


    /* Defender Options */

    @FXML
    ComboBox<String> defenderRocketTypeComboBox;

    @FXML
    ComboBox<String> defenderStartComboBox;

    @FXML
    Button defenderSpawnButton;

    @FXML
    ToggleButton defenderAutomaticModeButton;

    @FXML
    Slider defenderLauchSpeedSlider;

    @FXML
    Slider defenderRocketAmountSlider;

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

    Vector2D lastMouseCoordinates;

    public void init(MainViewModel mainViewModel) {
        this.mainViewModel = mainViewModel;

        entityView.getEntity().bindBidirectional(mainViewModel.getSelectedEntity());

        mapView.getWorldModel().setValue(mainViewModel.getWorldModel());
        mapView.getCenterWorldX().bind(mainViewModel.getCenterWorldX());
        mapView.getCenterWorldY().bind(mainViewModel.getCenterWorldY());
        mapView.getZoom().bind(mainViewModel.getZoom());
        mapView.getWorldWidth().bind(mainViewModel.getWorldWidth());
        mapView.getWorldHeight().bind(mainViewModel.getWorldHeight());

        mapView.prefWidthProperty().bind(stackPane.widthProperty());
        mapView.prefHeightProperty().bind(stackPane.heightProperty());

        attackerSpeedSlider.valueProperty().bindBidirectional(mainViewModel.getAttackerSpeed());
        attackerErrorStrengthSlider.valueProperty().bindBidirectional(mainViewModel.getAttackerErrorStrength());
        attackerRocketAmountSlider.valueProperty().bindBidirectional(mainViewModel.getAttackerRocketAmount());

        attackerStartComboBox.itemsProperty().bindBidirectional(mainViewModel.getAttackerStartSelectables());
        attackerTargetComboBox.itemsProperty().bindBidirectional(mainViewModel.getAttackerTargetSelectables());
        defenderStartComboBox.itemsProperty().bindBidirectional(mainViewModel.getDefenderStartSelectables());
        defenderRocketTypeComboBox.itemsProperty().bindBidirectional(mainViewModel.getDefenderRocketTypeSelectables());

        attackerStartComboBox.valueProperty().bindBidirectional(mainViewModel.getAttackerStartSelection());
        attackerTargetComboBox.valueProperty().bindBidirectional(mainViewModel.getAttackerTargetSelection());
        defenderStartComboBox.valueProperty().bindBidirectional(mainViewModel.getDefenderStartSelection());
        defenderRocketTypeComboBox.valueProperty().bindBidirectional(mainViewModel.getDefenderRocketTypeSelection());

        defenderAutomaticModeButton.selectedProperty().bindBidirectional(mainViewModel.getDefenderAutomaticMode());
        defenderSpawnButton.disableProperty().bind(defenderAutomaticModeButton.selectedProperty());

        defenderLauchSpeedSlider.valueProperty().bindBidirectional(mainViewModel.getDefenderLaunchSpeed());
        defenderRocketAmountSlider.valueProperty().bindBidirectional(mainViewModel.getDefenderRocketsPerThreat());

    }

    public void onAttackerSpawnButton(ActionEvent actionEvent) {
        mainViewModel.spawnAttackerRockets();
    }

    public void onDefenderSpawnButton(ActionEvent actionEvent) {
        mainViewModel.spawnDefenderRockets();
    }

    Vector2D dragOffset = Vector2D.ZERO();
    public void onMapViewDragged(MouseEvent mouseEvent) {
        if (!mouseEvent.isSecondaryButtonDown() && !mouseEvent.isMiddleButtonDown()) {
            return;
        }
        Vector2D coordinates = new Vector2D(mouseEvent.getX(), mouseEvent.getY());
        Vector2D delta = coordinates.sub(lastMouseCoordinates);
        lastMouseCoordinates = coordinates;
        dragOffset = dragOffset.add(delta.scale(-1));

        Util.batch(String.valueOf(this.hashCode()), () -> Platform.runLater(() -> {
            mainViewModel.dragMap(dragOffset.x, dragOffset.y);
            dragOffset = Vector2D.ZERO();
        }), 60);
    }

    public void onMapViewPressed(MouseEvent mouseEvent) {
        lastMouseCoordinates = new Vector2D(mouseEvent.getX(), mouseEvent.getY());
    }

    public void onMapViewScroll(ScrollEvent scrollEvent) {
        mainViewModel.zoomMap(scrollEvent.getDeltaY() / 400);
    }

    public void onMapViewClicked(MouseEvent mouseEvent) {
        if (!mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
            return;
        }
        Vector2D clickCoordinates = new Vector2D(mouseEvent.getX(), mouseEvent.getY());
        mainViewModel.selectEntityAtCoordinates(mapView.canvasToWorldCoordinates(clickCoordinates), Math.max(30, mapView.canvasToWorldLength(30)));
    }
}
