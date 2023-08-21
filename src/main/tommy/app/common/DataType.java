package app.common;

import javafx.scene.control.Label;

public enum DataType {
    ctrl, main;
    public Label getLabel() {
        return AppBuilder.labelWithClass(name(), "working-data-type");
    }
}
