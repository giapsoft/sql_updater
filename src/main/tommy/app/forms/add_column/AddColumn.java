package app.forms.add_column;

import app.common.AppBuilder;
import app.common.AppState;
import app.models.Column;
import app.models.Table;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import util.StringUt;
import util.Validator;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AddColumn extends AppBuilder {

    @FXML
    TextArea textAreaColumnName;

    @FXML
    Label labelError;

    List<String> columns = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        labelError.setText("");
        textAreaColumnName.textProperty().addListener((observable, oldValue, newValue) -> {
            validate();
        });
    }

    boolean validate() {
        String text = textAreaColumnName.getText().trim();
        List<String> rawCols = Arrays.stream(text.split(",")).collect(Collectors.toList());
        String error = rawCols.stream().map(Validator::validateTableName).filter(s -> !s.isEmpty())
                .collect(Collectors.joining(", "));
        if (!error.isEmpty()) {
            labelError.setText(error);
        } else {
            Table table = AppState.get.selection.getTable();
            String existed = rawCols.stream().map(StringUt::lower_underline).filter(table::hasColumn).collect(Collectors.joining(", "));
            if (!existed.isEmpty()) {
                labelError.setText("Column " + existed + " already existed");
            } else {
                columns = rawCols.stream().map(StringUt::lower_underline).collect(Collectors.toList());
                labelError.setText("");
            }
        }
        return labelError.getText().isEmpty();
    }

    public void submit() {
        if (validate() && !columns.isEmpty()) {
            columns.forEach(c -> {
                AppState.get.addColumn(new Column(
                        AppState.get.selection.getDataType(),
                        AppState.get.selection.getTable().getName(),
                        c));
            });
            close();
        }
    }
}
