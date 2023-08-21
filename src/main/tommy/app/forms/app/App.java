package app.forms.app;

import app.common.*;
import app.forms.add_category.AddCategory;
import app.forms.add_column.AddColumn;
import app.forms.add_table.AddTable;
import app.forms.search_row.SearchRow;
import app.forms.update_row.UpdateRow;
import app.icons.Icons;
import app.models.Column;
import app.models.Row;
import app.models.Table;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import lombok.SneakyThrows;
import setup.Config;

import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class App extends AppBuilder {
    @FXML
    ToggleGroup toggleGroupDataType;
    @FXML
    RadioButton ctrl;
    @FXML
    RadioButton main;
    @FXML
    VBox leftPanel;
    @FXML
    HBox dataTypeItems;

    @FXML
    ListView<String> listViewTables;

    @FXML
    Label labelOtherColumnNames;
    @FXML
    Label labelPkNames;
    @FXML
    Label labelSelectedTable;
    @FXML
    VBox vBoxWorkingItems;
    @FXML
    ScrollPane scrollPaneCommitting;
    @FXML
    VBox vBoxAddingCategories;
    @FXML
    FlowPane flowPaneAddingCategories;

    @FXML
    VBox vBoxAddingTables;
    @FXML
    VBox vBoxAddingTablesDet;

    @FXML
    VBox vBoxAddingColumns;
    @FXML
    VBox vBoxAddingColumnsDet;

    @FXML
    VBox vBoxUpdatingRows;
    @FXML
    VBox vBoxUpdatingRowsDet;

    @FXML
    VBox vBoxDeletingRows;
    @FXML
    VBox vBoxDeletingRowsDet;

    @FXML
    VBox vBoxWorkingTab;
    @FXML
    HBox hBoxWorkingActions;

    @FXML
    Button btnAddTable;
    @FXML
    Button btnAddColumn;
    @FXML
    Button btnUpdateRow;
    @FXML
    Button btnDeleteRow;
    @FXML
    Button btnSearchRow;

    @FXML
    Button buttonCommit;

    @FXML
    TextArea textAreaCommitMessage;

    @FXML
    TextField textFieldSearchTable;

    @SneakyThrows
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        listenAppStates();
        initUiActions();
        initUiFromAppHistory();
        initUiFromWorking();
        AppState.get.selectDataType(DataType.ctrl);
        labelWorkingDir.setText(Config.get.workingDir);
        labelSvnDir.setText(Config.get.svnDir);
    }

    private void initUiFromWorking() {
        if (Working.get.totalItems() < 1000) {
            Working.get.getAddingTables().forEach((dataType, tableMap) -> tableMap.forEach((tableName, table) -> addTableToWorking(table)));
            Working.get.getAddingColumns().forEach((id, names) -> names.forEach(name -> {
                Column column = Column.fromTableId(id, name);
                addColumnToWorking(column);
            }));
            Working.get.getAddingCategories().forEach(this::addCategoryToWorking);
            Working.get.getDeletingRows().forEach(this::addDeleteRowToWorking);
            Working.get.getUpdatingRows().forEach((s, row) -> this.addRowToWorking(row));

        } else {
            VBox vBox = new VBox();
            vBox.setSpacing(16);
            vBox.setPadding(new Insets(8, 8, 8, 8));
            vBox.getChildren().add(new Label("Cannot commit More than 1000 items, please try to reset and do again!"));
            Button resetBtn = new Button("reset");
            resetBtn.setOnMouseClicked(w -> {
                Working.get.reset();
                scrollPaneCommitting.setContent(vBoxWorkingItems);
            });
            vBox.getChildren().add(resetBtn);
            scrollPaneCommitting.setContent(vBox);
        }
    }

    private void initUiFromAppHistory() {
        AppHistory.get.getAllTables().getOrDefault(AppState.get.selection.getDataType(), new HashMap<>())
                .keySet().forEach(listViewTables.getItems()::add);
    }

    private void initUiActions() {
        ctrl.setOnAction(a -> AppState.get.selectDataType(DataType.ctrl));
        main.setOnAction(a -> AppState.get.selectDataType(DataType.main));
        textFieldSearchTable.textProperty().addListener(l -> reloadTableList());
        Icons.add.set(btnAddTable, btnAddColumn);
        Icons.edit.set(btnUpdateRow);
        Icons.trash.set(btnDeleteRow);
        Icons.search.set(btnSearchRow);
        Icons.ok.set(buttonCommit);
        vBoxWorkingTab.getChildren().remove(hBoxWorkingActions);
        vBoxWorkingItems.getChildren().clear();
        ctrl.setSelected(true);
    }

    public void onSelectTable() {
        String tableName = listViewTables.getSelectionModel().getSelectedItem();
        AppState.get.selectTableByName(tableName);
    }

    private void reloadTableList() {
        listViewTables.getItems().clear();
        listViewTables.getItems().addAll(AppState.get.data.findTableNames(AppState.get.selection.getDataType(), textFieldSearchTable.getText()));
    }

    private void listenAppStates() {
        AppState.get.listenAddCategory(this::addCategoryToWorking);

        AppState.get.listenAddColumn(c -> {
            if (Working.get.isAddingTable(c)) {
                addColumnToTable(c);
            } else {
                addColumnToWorking(c);
            }
            AppState.get.selectTable(c.getTable());
        });


        AppState.get.listenSelectDataType(dataType -> {
            textFieldSearchTable.setText("");
            listViewTables.getItems().clear();
            listViewTables.getItems().addAll(AppState.get.data.findTableNames(dataType, ""));
        });

        AppState.get.listenAddTable(this::addTableToWorking);
        AppState.get.listenRevertAddTable((dataType, s) -> {
            if (dataType == AppState.get.selection.getDataType()) {
                listViewTables.getItems().remove(s);
            }
        });

        AppState.get.listenSelectTable((dataType, table) -> {
            if (table == null) {
                vBoxWorkingTab.getChildren().remove(hBoxWorkingActions);
            } else {
                if (!vBoxWorkingTab.getChildren().contains(hBoxWorkingActions)) {
                    vBoxWorkingTab.getChildren().add(1, hBoxWorkingActions);
                }
            }
            labelSelectedTable.setText(table == null ? "None" : table.getName());
            labelPkNames.setText(table == null ? "Empty" : String.join(", ", table.getPkColumns()));
            labelOtherColumnNames.setText(table == null ? "Empty" : String.join(", ", table.getOtherColumns()));
        });

        AppState.get.listenUpdateRow(this::addRowToWorking);
        AppState.get.listenDeleteRow(this::addDeleteRowToWorking);
    }

    public void showAddTableDialog() {
        AppBuilder.showDialog(AddTable.class);
    }

    public void showAddCategoryDialog() {
        AppBuilder.showDialog(AddCategory.class);
    }

    public void showAddColumnDialog() {
        AppBuilder.showDialog(AddColumn.class);
    }

    public void showUpdateRowDialog() {
        AppState.get.selection.setRowAction(RowAction.add);
        AppBuilder.showDialog(UpdateRow.class);
    }

    public void showDeleteRowDialog() {
        AppState.get.selection.setRowAction(RowAction.delete);
        AppBuilder.showDialog(UpdateRow.class);
    }

    void addCategoryToWorking(String name) {
        HBox box = hBoxWithRevertBtn((b) -> {
            AppState.get.revertAddCategory(name);
            flowPaneAddingCategories.getChildren().remove(b);
            if (flowPaneAddingCategories.getChildren().isEmpty()) {
                vBoxWorkingItems.getChildren().remove(vBoxAddingCategories);
            }
        }, new Label(name));
        if (!vBoxWorkingItems.getChildren().contains(vBoxAddingCategories)) {
            vBoxWorkingItems.getChildren().add(0, vBoxAddingCategories);
        }

        flowPaneAddingCategories.getChildren().add(box);
    }

    void addDeleteRowToWorking(String rowId) {
        HBox box = hBoxWithRevertBtn((b) -> {
            AppState.get.revertDeleteRow(rowId);
            vBoxDeletingRowsDet.getChildren().remove(b);
            if (vBoxDeletingRowsDet.getChildren().isEmpty()) {
                vBoxWorkingItems.getChildren().remove(vBoxDeletingRows);
            }
        }, Row.getIdLabel(rowId));
        if (!vBoxWorkingItems.getChildren().contains(vBoxDeletingRows)) {
            vBoxWorkingItems.getChildren().add(vBoxDeletingRows);
        }
        vBoxDeletingRowsDet.getChildren().add(box);
    }

    Map<String, HBox> rowBoxes = new HashMap<>();
    Map<String, Runnable> rowReloadBoxes = new HashMap<>();

    void addRowToWorking(Row row) {
        try {
            HBox box = rowBoxes.computeIfAbsent(row.getId(), r -> createUpdatingRowBox(row));
            rowReloadBoxes.get(row.getId()).run();
            if (!vBoxUpdatingRowsDet.getChildren().contains(box)) {
                vBoxUpdatingRowsDet.getChildren().add(box);
            }
            if (!vBoxWorkingItems.getChildren().contains(vBoxUpdatingRows)) {
                vBoxWorkingItems.getChildren().add(vBoxUpdatingRows);
            }
        } catch (Exception ex) {
            System.out.println("Not Critical Exception: " + ex.getMessage());
        }

    }

    HBox createUpdatingRowBox(Row row) {
        HBox rowLabel = AppBuilder.hBox();
        rowLabel.getChildren().addAll(row.getFullLabelNodes());
        HBox box = hBoxWithRevertBtn(b -> {
                    AppState.get.revertUpdateRow(row.getId());
                    vBoxUpdatingRowsDet.getChildren().remove(b);
                    if (vBoxUpdatingRowsDet.getChildren().isEmpty()) {
                        vBoxWorkingItems.getChildren().remove(vBoxUpdatingRows);
                    }
                },
                Icons.edit.button(() -> {
                    try {
                        AppState.get.selection.setRowAction(RowAction.reviewUpdate);
                        AppState.get.selection.setUpdatingRow(Working.get.findUpdatingRow(row.getId()));
                        AppBuilder.showDialog(UpdateRow.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }),
                rowLabel);
        rowBoxes.put(row.getId(), box);
        rowReloadBoxes.put(row.getId(), () -> {
            rowLabel.getChildren().clear();
            rowLabel.getChildren().addAll(row.getFullLabelNodes());
        });
        return box;
    }

    Map<String, Label> addingTableColumns = new HashMap<>();

    void addTableToWorking(Table table) {
        listViewTables.getItems().add(0, table.getName());
        listViewTables.getSelectionModel().select(table.getName());
        DataType dataType = table.getDataType();
        java.util.List<Control> controls = new ArrayList<>();
        controls.add(dataType.getLabel());
        controls.add(new Label("."));
        controls.add(table.getNameLabel());
        controls.add(new Label("."));
        controls.add(table.getPkLabel());
        controls.add(new Label("."));
        Label otherLabel = table.getOtherLabel();
        addingTableColumns.put(table.getId(), otherLabel);
        controls.add(otherLabel);
        HBox box = hBoxWithRevertBtn(b -> {
            AppState.get.revertAddTable(dataType, table.getName());
            vBoxAddingTablesDet.getChildren().remove(b);
            if (vBoxAddingTablesDet.getChildren().isEmpty()) {
                vBoxWorkingItems.getChildren().remove(vBoxAddingTables);
            }
        }, controls.toArray(new Control[0]));
        if (!vBoxWorkingItems.getChildren().contains(vBoxAddingTables)) {
            vBoxWorkingItems.getChildren().add(vBoxAddingTables);
        }
        vBoxAddingTablesDet.getChildren().add(box);
    }

    void addColumnToTable(Column column) {
        Table table = column.getTable();
        addingTableColumns.get(table.getId()).setText(String.format(".other<%s>", String.join(", ", table.getOtherColumns())));
    }

    Map<String, Pair<HBox, HBox>> hBoxAddingColumnsMap = new HashMap<>();

    void addColumnToWorking(final Column column) {
        final Runnable reloadTable = () -> AppState.get.selectTable(column.getTable());
        Runnable initColumn = () -> {
            DataType dataType = column.getDataType();
            Table table = AppState.get.data.getTable(dataType, column.getTableName());
            List<Node> nodes = new ArrayList<>();
            nodes.add(table.getIdLabel());
            HBox motherBox = new HBox();
            nodes.add(motherBox);
            motherBox.setMinWidth(300);
            HBox grandmaBox = hBoxWithRevertBtn(b -> {
                AppState.get.removeAllColumns(column);
                motherBox.getChildren().clear();
                vBoxAddingColumnsDet.getChildren().remove(b);
                vBoxWorkingItems.getChildren().remove(vBoxAddingColumns);
                reloadTable.run();
            }, nodes.toArray(new Node[0]));
            vBoxAddingColumnsDet.getChildren().add(grandmaBox);
            hBoxAddingColumnsMap.put(column.getTableId(), new Pair<>(grandmaBox, motherBox));
        };

        Supplier<Pair<HBox, HBox>> getPair = () -> {
            if (!hBoxAddingColumnsMap.containsKey(column.getTableId())) {
                initColumn.run();
            }
            return hBoxAddingColumnsMap.get(column.getTableId());
        };

        HBox grandmaBox = getPair.get().getKey();
        HBox motherBox = getPair.get().getValue();
        Set<String> addingCols = Working.get.findAddingColumns(column);
        AppState.get.selectTable(column.getTable());
        if (!vBoxAddingColumnsDet.getChildren().contains(grandmaBox)) {
            vBoxAddingColumnsDet.getChildren().add(grandmaBox);
        }

        if (addingCols.isEmpty()) {
            vBoxWorkingItems.getChildren().remove(vBoxAddingColumns);
        } else if (!vBoxWorkingItems.getChildren().contains(vBoxAddingColumns)) {
            vBoxWorkingItems.getChildren().add(vBoxAddingColumns);
        }

        HBox childBox = hBoxWithRevertBtn(b -> {
            AppState.get.removeColumn(column);
            motherBox.getChildren().remove(b);
            if (motherBox.getChildren().isEmpty()) {
                vBoxAddingColumnsDet.getChildren().remove(grandmaBox);
            }
            if (vBoxAddingColumnsDet.getChildren().isEmpty()) {
                vBoxWorkingItems.getChildren().remove(vBoxAddingColumns);
            }
            reloadTable.run();
        }, column.getLabel());

        HBox.setMargin(childBox, new Insets(0, 4, 0, 4));
        motherBox.getChildren().add(childBox);
        reloadTable.run();
    }


    HBox hBoxWithRevertBtn(Consumer<HBox> onRevert, Node... controls) {
        HBox box = new HBox();
        box.setAlignment(Pos.CENTER_LEFT);
        box.getStyleClass().add("working-item-box");

        Button button = Icons.revert.button();
        button.setOnMouseClicked(l -> onRevert.accept(box));
        box.getChildren().add(button);

        HBox subBox = new HBox();
        subBox.setAlignment(Pos.CENTER_LEFT);

        subBox.getChildren().addAll(controls);
        box.getChildren().add(subBox);
        subBox.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        subBox.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        box.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        box.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        return box;
    }

    @FXML
    VBox container;

    public void commit() {
setDisable(true);
        new Thread(() -> {
            if (vBoxWorkingItems.getChildren().isEmpty()) {
                AppBuilder.showError("Nothing To Commit", "Please dome some edit then try to commit again!");
                setDisable(false);
                return;
            }
            String message = textAreaCommitMessage.getText();
            if (!message.startsWith("Task ID:")) {
                AppBuilder.showError("Invalid Commit Message", "Commit Message must be started with 'Task ID:'");
                setDisable(false);
                return;
            }
            try {
                Working.get.commit(message);
                resetWorkingSpace();
                setDisable(false);
                AppBuilder.showInfo("Done", "Committed successfully!");
            } catch (Exception ex) {
                AppBuilder.showError("Oops, Something went wrong!", "Please: " +
                        "\n\t1. Cleanup " +
                        "\n\t2. Update to latest revision" +
                        "\n\t3. Open this app again!");
                System.exit(0);
            }

        }).start();
    }

    private void resetWorkingSpace() {
        Platform.runLater(() -> {
            flowPaneAddingCategories.getChildren().clear();
            vBoxAddingTablesDet.getChildren().clear();
            vBoxAddingColumnsDet.getChildren().clear();
            vBoxUpdatingRowsDet.getChildren().clear();
            vBoxDeletingRowsDet.getChildren().clear();
            vBoxWorkingItems.getChildren().clear();
            rowBoxes.clear();
            addingTableColumns.clear();
            hBoxAddingColumnsMap.clear();
            textAreaCommitMessage.setText("");
        });
    }

    void setDisable(boolean disable) {
        Platform.runLater(() -> {
            for (Node node : getAllNodes(container)) {
                node.setDisable(disable);
            }
        });
    }

    @FXML
    Label labelWorkingDir;
    @FXML
    Label labelSvnDir;
    @FXML
    Button btnOpenWorkingDir;
    @FXML
    Button btnOpenSvnDir;

    public void searchRow() {
        AppBuilder.showDialog(SearchRow.class);
    }

}
