package main.java.model;

import lombok.Getter;

public class ModelFactory {

    @Getter
    WorldModel worldModel;

    public ModelFactory ()  {
        this.worldModel = new DefaultWorldModel(100, 100, 1);
    }
}
