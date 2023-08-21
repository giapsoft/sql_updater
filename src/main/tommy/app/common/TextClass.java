package app.common;


import javafx.scene.control.Label;

public enum TextClass {
    error, success, main, ctrl, help;
    public void set(Label label) {
        label.getStyleClass().clear();
        label.getStyleClass().add("text-" + this.name());
    }
}
