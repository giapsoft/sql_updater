package app.models;

import app.common.AppBuilder;
import app.common.AppState;
import app.common.DataType;
import javafx.scene.control.Label;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Column {
    DataType dataType;
    String tableName;
    String columnName;

    public boolean isOf(DataType dataType, String name) {
        return this.dataType == dataType && this.tableName.equals(name);
    }

    public Table getTable() {
        return AppState.get.data.getTable(dataType, tableName);
    }

    public String getTableId() {
        return String.format("%s.%s", dataType.name(), tableName);
    }

    public static Column fromTableId(String tableId, String columnName) {
        String[] parts = tableId.split("\\.");
        DataType dataType = DataType.valueOf(parts[0]);
        String tableName = parts[1];
        return new Column(dataType, tableName, columnName);
    }

    public Column createColumn(String name) {
        return new Column(dataType, tableName, name);
    }

    public Label getLabel() {
        return AppBuilder.labelWithClass(columnName, "working-other-columns");
    }
}
