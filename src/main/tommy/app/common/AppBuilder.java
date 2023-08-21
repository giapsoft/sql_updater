package app.common;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import setup.Config;
import util.FileUt;
import util.StringUt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class AppBuilder implements Initializable {
    public static final Map<Class, Runnable> closeDialogs = new HashMap<>();

    public static <T extends AppBuilder> void showDialog(Class<T> clazz) {
        try {
            Parent root = getAsParent(clazz);
            final Stage dialog = new Stage();
            closeDialogs.put(clazz, dialog::close);
            dialog.setScene(new Scene(root));
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(StringUt.capSpace(clazz.getSimpleName()));
            dialog.showAndWait();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
        }

    }

    public static void message(Alert.AlertType type, String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(content);
            alert.showAndWait();
        });
    }

    public static void showError(String title, String content) {
        message(Alert.AlertType.ERROR, title, content);
    }

    public static void showInfo(String title, String content) {
        message(Alert.AlertType.INFORMATION, title, content);
    }

    public static Parent getAsParent(Class<?> clazz) throws IOException {
        return FXMLLoader.load(Objects.requireNonNull(clazz.getResource(StringUt.lower_underline(clazz.getSimpleName()) + ".fxml")));
    }

    public void close() {
        closeDialogs.get(this.getClass()).run();
    }

    public static Label labelWithClass(String text, String clazz) {
        Label label = new Label(text);
        label.getStyleClass().add(clazz);
        label.setMinWidth(Region.USE_COMPUTED_SIZE);
        return label;
    }

    public static HBox hBox(Node... nodes) {
        HBox subBox = new HBox();
        subBox.setSpacing(4);
        subBox.setPadding(new Insets(0, 4, 0, 4));
        subBox.setAlignment(Pos.CENTER_LEFT);
        subBox.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        if (nodes != null) {
            subBox.getChildren().addAll(nodes);
        }
        return subBox;
    }

    public void openWorkingDir() throws IOException {
        FileUt.openDir(Config.get.workingDir);
    }

    public void openSvnDir() throws IOException {
        FileUt.openDir(Config.get.svnDir);
    }

    public static ArrayList<Node> getAllNodes(Parent root) {
        ArrayList<Node> nodes = new ArrayList<Node>();
        addAllDescendents(root, nodes);
        return nodes;
    }

    private static void addAllDescendents(Parent parent, ArrayList<Node> nodes) {
        for (Node node : parent.getChildrenUnmodifiable()) {
            nodes.add(node);
            if (node instanceof Parent)
                addAllDescendents((Parent)node, nodes);
        }
    }
}
