package app.icons;

import javafx.css.StyleableProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;


@SuppressWarnings("all")
public enum Icons {
    add("+"), revert, delete, edit, trash, eye("View"), ok, search,
    user, date, message, id

    ;

    Icons() {

    }

    Icons(String character) {
        this.character = character;
    }

    String character = "";

    public void set(Labeled... nodes) {
        for (Labeled node : nodes) {
            node.setText(node.getText().replace(character, ""));
            Image imageOk = new Image(Objects.requireNonNull(Icons.class.getResourceAsStream(name() + ".png")));
            ((StyleableProperty<javafx.scene.Node>) node.graphicProperty()).applyStyle(null, new ImageView(imageOk));
        }

    }

    public Button button() {
        Button button = new Button();
        set(button);
        return button;
    }

    public Button button(Runnable onClick) {
        Button button = button();
        button.setOnMouseClicked(m -> onClick.run());
        return button;
    }

    public Label label(String text) {
        Label label = new Label();
        set(label);
        label.setText(text);
        return label;
    }
}
