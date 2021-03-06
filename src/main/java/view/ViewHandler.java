package main.java.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;
import main.java.view.main.MainViewController;
import main.java.viewmodel.ViewModelFactory;

import java.io.IOException;

public class ViewHandler {
    private Stage stage;
    private ViewModelFactory viewModelFactory;
    @Getter
    private Parent root;

    public ViewHandler(Stage stage, ViewModelFactory viewModelFactory) {
        this.stage = stage;
        this.viewModelFactory = viewModelFactory;
    }

    public void start() throws IOException {
        Scene scene;
        FXMLLoader loader = new FXMLLoader();

        loader.setLocation(getClass().getResource("/MainView.fxml"));
        root = loader.load();

        MainViewController view = loader.getController();
        view.init(viewModelFactory.getMainViewModel());
        stage.setTitle("MainView");

        scene = new Scene(root);
        stage.setScene(scene);

        stage.minWidthProperty().set(600);
        stage.minHeightProperty().set(600);
        ;

        stage.show();

    }

}
