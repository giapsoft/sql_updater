package app.models;

import app.common.AppBuilder;
import app.common.AppState;
import app.common.DataType;
import app.forms.update_row.UpdateRow;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import util.Encrypt;
import util.StringUt;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Data
public class Row {

    public String toSql() {
        if (sqlAction == SqlAction.insert) {
            return toInsert();
        } else if (sqlAction == SqlAction.update) {
            return toUpdate();
        } else {
            return toDelete();
        }
    }

    public enum SqlAction {
        insert, update, delete;
    }

    final String categoryName;
    final DataType dataType;
    final String tableName;
    final Map<String, String> columns;
    boolean isDeleted = false;
    SqlAction sqlAction;

    public List<String> search(String searchKey) {
        return columns.entrySet().stream().filter(c -> StringUt.isSearchContains(c.getValue(), searchKey)).map(Map.Entry::getKey).collect(Collectors.toList());
    }

    public String get(String colName) {
        return columns.get(colName);
    }

    public void set(String colName, String value) {
        columns.put(colName, value);
    }

    public String getId() {
        return String.format("%s.%s.%s.%s", categoryName, dataType, tableName, getPkJson());
    }

    public void remove(String name) {
        columns.remove(name);
    }

    Table table;
    public Table getTable() {
        return table != null ? table : AppState.get.data.getTable(getDataType(), getTableName());
    }

    public void setTable(Table table) {
        this.table = table;
    }

    String getPkJson() {
        Map<String, String> pk = new HashMap<>();
        for (String pkColumn : getTable().getPkColumns()) {
            pk.put(pkColumn, columns.get(pkColumn));
        }
        return Encrypt.toJson(pk);
    }

    String getOthersJson() {
        Map<String, String> columns = new HashMap<>();
        for (String colName : getTable().getOtherColumns()) {
            columns.put(colName, this.columns.get(colName));
        }
        return Encrypt.toJson(columns);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Row decodeId(String id) {
        String[] parts = id.split("\\.");
        String catName = parts[0];
        DataType dataType = DataType.valueOf(parts[1]);
        String tableName = parts[2];
        String pkJson = id.substring(catName.length() + dataType.name().length() + tableName.length() + 2);
        Map pk = Encrypt.fromJson(pkJson, Map.class);
        return new Row(catName, dataType, tableName, pk);
    }

    public List<Node> getFullLabelNodes() {
        return Arrays.asList(AppBuilder.labelWithClass(categoryName, "working-category"),
                new Label("."),
                dataType.getLabel(),
                new Label("."),
                getTable().getNameLabel(),
                new Label("."),
                AppBuilder.labelWithClass(getPkJson(), "working-pk-columns"),
                new Label("."),
                AppBuilder.labelWithClass(getOthersJson(), "working-other-columns")
        );
    }

    public static HBox getIdLabel(String id) {
        HBox subBox = AppBuilder.hBox();
        String[] parts = id.split("\\.");
        subBox.getChildren().addAll(AppBuilder.labelWithClass(parts[0], "working-category"),
                new Label("."),
                AppBuilder.labelWithClass(parts[1], "working-data-type"),
                new Label("."),
                AppBuilder.labelWithClass(parts[2], "working-table-name"),
                new Label("."),
                AppBuilder.labelWithClass(parts[3], "working-pk-columns"));
        return subBox;
    }

    public void merge(Row row) {
        if (isDeleted) {
            columns.clear();
            row.columns.forEach(columns::put);
            isDeleted = row.isDeleted;
        }
    }

    public String toInsert() {
        java.util.List<String> colNames = new ArrayList<>(columns.keySet());
        List<String> values = colNames.stream().map(columns::get).map(s -> String.format("'%s'", s)).collect(Collectors.toList());
        return String.format("INSERT INTO %s (%s) VALUES (%s);", tableName, java.lang.String.join(", ", colNames), String.join(", ", values));
    }

    private String toUpdate() {
        String pkSelect = getTable().getPkColumns().stream().map(pk -> String.format("%s = '%s'", pk, columns.get(pk))).collect(Collectors.joining(" AND "));
        String columnSetter = getTable().getOtherColumns().stream().filter(columns::containsKey)
                .map(s -> String.format("%s = %s", s, UpdateRow.isNull(columns.get(s)) ? "null" : "'" + s + "'"))
                .collect(Collectors.joining(", "));
        return String.format("UPDATE %s SET %s WHERE %s;", tableName, columnSetter, pkSelect);
    }

    private String toDelete() {
        String pkSelect = getTable().getPkColumns().stream().map(pk -> String.format("%s = '%s'", pk, columns.get(pk))).collect(Collectors.joining(" AND "));
        return String.format("DELETE FROM %s WHERE %s;", tableName, pkSelect);
    }
}
