package app.forms.search_row;

import app.common.AppBuilder;
import app.common.AppState;
import app.common.RowAction;
import app.forms.update_row.UpdateRow;
import app.models.Row;
import app.models.Table;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.layout.VBox;
import org.tmatesoft.svn.core.SVNException;
import setup.Config;
import util.Svn;
import util.Timer;

import java.net.URL;
import java.util.*;

public class SearchRow extends AppBuilder {


    @FXML
    VBox container;

    @FXML
    Label labelStatus;

    @FXML
    TextField textFieldSearch;

    @FXML
    TableView<Map<String, String>> tableViewRows;

    Timer timer;

    @FXML
    Label labelTableName;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tableViewRows.setPlaceholder(new Label("No Row Found."));
        container.getChildren().remove(tableViewRows);
        tableViewRows.getSelectionModel().selectedItemProperty().addListener((obs, old, current) -> {
            AppState.get.selection.setRowAction(RowAction.update);
            AppState.get.selection.setUpdatingRow(new Row(AppState.generalCat, AppState.get.selection.getDataType(), table.getName(), current));
            AppBuilder.showDialog(UpdateRow.class);
        });
        textFieldSearch.setPromptText("loading...");
        labelStatus.setText("loading...");
        init();
        textFieldSearch.setDisable(true);
        textFieldSearch.textProperty().addListener(l -> {
            if (timer != null) {
                timer.cancel();
            }
            timer = new Timer(this::search);
        });
    }

    static long lastRev = 0;
    static Map<Table, Map<String, Row>> rows = new HashMap<>();

    Map<String, Row> tblRows;

    Table table;

    void init() {
        new Thread(() -> {
            try {
                table = AppState.get.selection.getTable();
                Platform.runLater(() -> labelTableName.setText(table.getName()));
                Svn.update(Config.get.svnDir);
                long svnLastRev = Svn.lastRev();
                if (lastRev < svnLastRev) {
                    lastRev = svnLastRev;
                    rows.clear();
                }

                tblRows = rows.computeIfAbsent(table, t -> table.loadRows());
                setStatus("loaded");
                Platform.runLater(() -> {
                    textFieldSearch.setDisable(false);
                    textFieldSearch.setPromptText("input text to search");
                    container.getChildren().add(1, tableViewRows);
                });
            } catch (SVNException e) {
                e.printStackTrace();
                AppBuilder.showError("Error", "Cannot update SVN at " + Config.get.svnDir);
            }
        }).start();
    }

    void setStatus(String text) {
        Platform.runLater(() -> labelStatus.setText(text));
    }

    List<Row> searchResult = new ArrayList<>();
    Set<String> columns = new TreeSet<>();
    ObservableList<Map<String, String>> items = FXCollections.observableArrayList();

    void search() {
        if (textFieldSearch.getText().trim().isEmpty()) {
            setStatus("please input text to search");
            return;
        }
        setStatus("filtering...");
        searchResult.clear();
        columns.clear();
        items.clear();
        columns.addAll(table.getPkColumns());

        updateTable();
        setStatus("done.");
    }

    private void updateTable() {
        Platform.runLater(() -> {
            tableViewRows.getColumns().clear();
            if (items.isEmpty()) {
                return;
            }
            for (String name : columns) {
                TableColumn<Map<String, String>, Object> column = new TableColumn<>(name);
                MapValueFactory factory = new MapValueFactory<>(name);
                column.setCellValueFactory(factory);
                tableViewRows.getColumns().add(column);
            }
            for (Map<String, String> item : items) {
                tableViewRows.getItems().add(item);
            }
        });
    }
}
