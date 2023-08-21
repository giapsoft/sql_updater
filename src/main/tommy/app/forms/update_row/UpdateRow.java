package app.forms.update_row;

import app.common.AppBuilder;
import app.common.AppState;
import app.common.RowAction;
import app.common.Working;
import app.models.Row;
import app.models.Table;
import app.icons.Icons;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import util.StringUt;

import java.net.URL;
import java.util.*;

public class UpdateRow extends AppBuilder {

    public static String NULL_VALUE = "NULL_VALUE";
    static Map<String, String> current;

    @FXML
    VBox vBoxTableName;

    @FXML
    ScrollPane scrollPaneFields;
    @FXML
    VBox vBoxFields;

    @FXML
    Label labelTableName;
    @FXML
    Label labelTableAction;
    @FXML
    Label labelError;

    public static boolean isNull(String s) {
        return NULL_VALUE.equals(s);
    }

    public boolean isDeleting() {
        return AppState.get.selection.getRowAction() == RowAction.delete;
    }

    public boolean isReviewUpdate() {
        return AppState.get.selection.getRowAction() == RowAction.reviewUpdate;
    }

    public boolean isAddingRow() {
        return AppState.get.selection.getRowAction() == RowAction.add;
    }

    Row row;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (isAddingRow() || isDeleting()) {
            row = new Row(AppState.get.selection.getCatName(), AppState.get.selection.getDataType(), AppState.get.selection.getTable().getName(), new HashMap<>());
        } else {
            row = AppState.get.selection.getUpdatingRow();
        }
        labelTableAction.setText(isDeleting() ? "Deleting Row on Table" : "Updating Row on Table");
        labelTableName.setText(row.getTableName());
        Table table = row.getTable();
        table.getPkColumns().forEach(p -> addColumn(p, true));
        if (!isDeleting()) {
            table.getOtherColumns().forEach(c -> addColumn(c, false));
        }

        leftLabels.forEach(label -> {
            label.setMinWidth(labelSize);
            label.setMaxWidth(labelSize);
            label.setAlignment(Pos.CENTER_RIGHT);
            label.setPadding(new Insets(4, 4, 4, 4));
        });
    }

    List<Label> leftLabels = new ArrayList<>();
    int labelSize = 0;


    void addColumn(String name, boolean isPk) {
        String colLabel = isPk ? name + " (*)" : name;
        if (colLabel.length() * 10 > labelSize) {
            labelSize = colLabel.length() * 10;
        }
        Label label = new Label(colLabel);
        leftLabels.add(label);
        VBox outer = new VBox();
        HBox inner = new HBox();
        inner.setAlignment(Pos.CENTER_LEFT);
        outer.getChildren().add(inner);
        inner.getChildren().add(label);
        inner.getChildren().add(createControls(name, isPk));
        HBox.setHgrow(outer, Priority.ALWAYS);
        vBoxFields.getChildren().add(outer);
    }

    boolean isValidToSubmit() {
        labelError.setText("");
        if (row.getTable().getPkColumns().stream().anyMatch(r -> StringUt.isEmptyTrim(row.get(r)))) {
            labelError.setText("Please input all valid info.");
        } else {
            if (isDeleting() && Working.get.isUpdatingRow(row.getId())) {
                labelError.setText("Revert updating first before delete this row");
            }
            if (isAddingRow() && Working.get.isDeletingRow(row.getId())) {
                labelError.setText("Revert deleting first before update this row");
            }
        }
        return StringUt.isEmptyTrim(labelError.getText());
    }

    public void submit() {
        if (isValidToSubmit()) {
            if (isDeleting()) {
                AppState.get.deleteRow(row.getId());
            } else {
                AppState.get.updateRow(row);
            }
            close();
        }
    }

    HBox createControls(String name, boolean isPk) {
        HBox controls = new HBox();
        controls.setSpacing(4);
        controls.setPadding(new Insets(0, 0, 0, 20));
        controls.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(controls, Priority.ALWAYS);
        Button editBtn = Icons.edit.button();
        Button revertBtn = Icons.revert.button();
        Button toNullBtn = new Button("to NULL");
        Label nullLabel = new Label("NULL");
        TextArea textArea = new TextArea();

        Runnable loadState = () -> {
            controls.getChildren().clear();

            String value = row.get(name);
            if (value == null) {
                controls.getChildren().add(editBtn);
                controls.getChildren().add(toNullBtn);
            } else if (value.equals(NULL_VALUE)) {
                textArea.setText(NULL_VALUE);
                controls.getChildren().add(nullLabel);
                controls.getChildren().add(revertBtn);
            } else {
                textArea.setText(value);
                controls.getChildren().add(textArea);
                controls.getChildren().add(revertBtn);
            }
        };

        textArea.textProperty().addListener(l -> {
            int lineCount = textArea.getText().split("\n").length;
            controls.setPrefHeight(lineCount * 18);
            row.set(name, textArea.getText());
        });
        textArea.setPrefHeight(20);
        HBox.setHgrow(textArea, Priority.ALWAYS);

        revertBtn.setOnMouseClicked(m -> {
            row.remove(name);
            loadState.run();
        });

        editBtn.setOnMouseClicked(m -> {
            row.set(name, "");
            loadState.run();
        });

        toNullBtn.setOnMouseClicked(m -> {
            row.set(name, NULL_VALUE);
            loadState.run();

        });

        if (isPk) {
            TextField pkField = new TextField();
            controls.getChildren().add(pkField);
            HBox.setHgrow(pkField, Priority.ALWAYS);
            pkField.textProperty().addListener(l -> row.set(name, pkField.getText()));
            pkField.setText(row.get(name));
            if (isReviewUpdate()) {
                pkField.setDisable(true);
            }
        } else {
            loadState.run();
        }
        return controls;
    }
}
