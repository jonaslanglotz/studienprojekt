package main.java;

import javafx.application.Application;
import javafx.stage.Stage;
import main.java.model.ModelFactory;
import main.java.view.ViewHandler;
import main.java.viewmodel.ViewModelFactory;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        ModelFactory modelFactory = new ModelFactory();
        ViewModelFactory viewModelFactory = new ViewModelFactory(modelFactory);
        ViewHandler viewHandler = new ViewHandler(primaryStage, viewModelFactory);
        viewHandler.start();
    }
}
