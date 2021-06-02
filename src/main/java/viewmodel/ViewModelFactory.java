package main.java.viewmodel;

import lombok.Getter;
import main.java.model.ModelFactory;

public class ViewModelFactory {
    ModelFactory modelFactory;

    @Getter
    MainViewModel mainViewModel;

    public ViewModelFactory(ModelFactory modelFactory) {
        this.modelFactory = modelFactory;
        this.mainViewModel = new MainViewModel(modelFactory.getWorldModel());
    }
}
