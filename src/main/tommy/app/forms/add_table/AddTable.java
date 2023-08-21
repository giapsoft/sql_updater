package app.forms.add_table;

import app.common.AppBuilder;
import app.common.AppState;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import util.StringUt;
import util.Validator;

import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class AddTable extends AppBuilder {

    @FXML
    TextField textFieldTableName;
    @FXML
    TextField textFieldPkColumns;
    @FXML
    TextArea textAreaOtherColumns;

    @FXML
    VBox vBoxTableName;
    @FXML
    VBox vBoxPkColumns;
    @FXML
    VBox vBoxOtherColumns;
    @FXML
    Label labelTableNameError;
    @FXML
    Label labelPkColumnsError;
    @FXML
    Label labelOtherColumnsError;


    String tableName = "";
    Set<String> pkColumns = new HashSet<>();
    Set<String> otherColumns = new HashSet<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        textFieldTableName.textProperty().addListener((ob, o, n) -> validateTableName());
        textFieldPkColumns.textProperty().addListener((ob, o, n) -> validatePkColumns());
        textAreaOtherColumns.textProperty().addListener((ob, o, n) -> validateOtherColumns());
        vBoxTableName.getChildren().remove(labelTableNameError);
        vBoxPkColumns.getChildren().remove(labelPkColumnsError);
        vBoxOtherColumns.getChildren().remove(labelOtherColumnsError);
    }

    boolean validateTableName() {
        vBoxTableName.getChildren().remove(labelTableNameError);
        String error = Validator.validateTableName(textFieldTableName.getText());
        String name = StringUt.lower_underline(textFieldTableName.getText());
        if (AppState.get.data.findTableNames(AppState.get.selection.getDataType(), "").contains(name)) {
            error = "Table " + name + " already existed";
        }
        labelTableNameError.setText(error);

        if (!error.isEmpty()) {
            vBoxTableName.getChildren().add(labelTableNameError);
        } else {
            tableName = name;
        }
        return error.isEmpty();
    }

    boolean validatePkColumns() {
        vBoxPkColumns.getChildren().remove(labelPkColumnsError);
        return validateColumns(textFieldPkColumns.getText(), otherColumns, (pk) -> pkColumns = pk, (error) -> {
            labelPkColumnsError.setText(error);
            vBoxPkColumns.getChildren().add(labelPkColumnsError);
        });
    }

    boolean validateOtherColumns() {
        vBoxOtherColumns.getChildren().remove(labelOtherColumnsError);
        if (StringUt.isEmptyTrim(textAreaOtherColumns.getText())) {
            return true;
        }
        return validateColumns(textAreaOtherColumns.getText(), pkColumns, (cols) -> otherColumns = cols, (error) -> {
            labelOtherColumnsError.setText(error);
            vBoxOtherColumns.getChildren().add(labelOtherColumnsError);
        });
    }

    boolean validateColumns(String input, Set<String> remainColumns, Consumer<Set<String>> onSuccess, Consumer<String> onError) {
        String text = StringUt.emptyTrim(input.trim());
        String error = "";
        String[] cols = text.split(",");
        Set<String> invalidCols = Arrays.stream(cols).map(Validator::validateTableName).filter(StringUt::isNotEmptyTrim).collect(Collectors.toSet());
        if (!invalidCols.isEmpty()) {
            error = String.join(", ", invalidCols);
        } else {
            Set<String> all = new HashSet<>(remainColumns);
            Set<String> duplicated = Arrays.stream(cols).map(StringUt::lower_underline).filter(c -> !all.add(c)).collect(Collectors.toSet());
            if (!duplicated.isEmpty()) {
                error = "Duplicated " + String.join(", ", duplicated);
            }
        }
        if (error.isEmpty()) {
            onSuccess.accept(Arrays.stream(cols).map(StringUt::lower_underline).collect(Collectors.toSet()));
        } else {
            onError.accept(error);
        }
        return error.isEmpty();
    }


    public void submit() {
        if (Validator.isValid(validateTableName(), validatePkColumns(), validateOtherColumns())) {
            AppState.get.createTable(tableName, pkColumns, otherColumns);
            close();
        }
    }
}
