package model;

import javafx.beans.property.SimpleObjectProperty;

import java.util.List;

/**
 * Created by alex on 20.04.15.
 */
public class Model {

    private List<Object> objects;

    public Model(List<Object> objects) {
        this.objects = objects;
    }

    public Object getObject(int i) {
        return objects.get(i);
    }
}
