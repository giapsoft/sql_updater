package app.models;

import app.common.AppBuilder;
import app.common.AppState;
import setup.Config;
import app.common.DataType;
import app.trackers.RowTracker;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Table {
    DataType dataType;
    String name;
    Set<String> pkColumns;
    Set<String> otherColumns;

    public boolean hasColumn(String name) {
        return pkColumns.contains(name) || otherColumns.contains(name);
    }

    public String getId() {
        return String.join(".", dataType.name(), name);
    }


    public Label getNameLabel() {
        return AppBuilder.labelWithClass(name, "working-table-name");
    }

    public Control getPkLabel() {
        return AppBuilder.labelWithClass(String.format("pk<%s>", String.join(", ", getPkColumns())), "working-pk-columns");
    }

    public Label getOtherLabel() {
        return AppBuilder.labelWithClass(String.format("other<%s>", String.join(", ", getOtherColumns())), "working-other-columns");
    }

    public HBox getIdLabel() {
        return AppBuilder.hBox(dataType.getLabel(),
                new Label("."),
                getNameLabel());
    }

    public String getFilePath(String catName) {
        return Config.get.tableFilePath(catName, getDataType(), getName());
    }

    public Map<String, Row> loadRows() {
        return loadRows(AppState.generalCat);
    }
    public Map<String, Row> loadRows(String categoryName) {
        List<RowTracker> trackers = RowTracker.parse(getFilePath(categoryName));
        return loadRows(categoryName, trackers);
    }

    public Map<String, Row> loadRows(String categoryName, String raw) {
        return loadRows(categoryName, RowTracker.parseFromRaw(raw));
    }

    public Map<String, Row> loadRows(String categoryName, List<RowTracker> trackers) {
        Map<String, Row> result = new HashMap<>();
        trackers.forEach(tracker -> {
            Row trackerRow = new Row(categoryName, dataType, name, tracker.getColumns());
            String id = trackerRow.getId();
            Row currentRow = result.get(id);
            if (currentRow == null) {
                result.put(id, trackerRow);
            } else {
                currentRow.merge(trackerRow);
            }
        });
        return result;
    }
}
