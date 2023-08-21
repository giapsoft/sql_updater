package app.common;

import app.models.Column;
import app.models.Table;
import app.trackers.AppHistoryTracker;
import setup.Config;
import util.Encrypt;
import util.FileUt;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class AppHistory {

    // datatype - tableName - table
    Map<DataType, Map<String, Table>> allTables = new HashMap<>();
    TreeSet<String> categories = new TreeSet<>();


    public static final AppHistory get = new AppHistory();

    public void reset() {
        FileUt.deleteFolder(Config.get.appHistoryFilePath);
        allTables.clear();
        categories.clear();
    }

    public void init() {
        String raw = FileUt.read(Config.get.appHistoryFilePath);
        List<AppHistoryTracker> trackers = Encrypt.decode(raw, AppHistoryTracker.class);
        for (AppHistoryTracker tracker : trackers) {
            categories.addAll(tracker.getAddingCategories());
            tracker.getAddingTables().forEach((dataType, stringTableMap) -> {
                stringTableMap.forEach((tableName, table) -> {
                    allTables.computeIfAbsent(dataType, d -> new HashMap<>())
                            .put(tableName, table);
                });
            });
            tracker.getAddingColumns().forEach((tableId, colNames) -> {
                colNames.forEach(name -> {
                    Column column = Column.fromTableId(tableId, name);
                    allTables.get(column.getDataType())
                            .get(column.getTableName())
                            .getOtherColumns().add(name);
                });
            });
        }
        if (categories.contains(AppState.generalCat)) {
            categories.add(AppState.generalCat);
        }
    }

    public TreeSet<String> allCatNames() {
        TreeSet<String> result = new TreeSet<>();
        result.add(AppState.generalCat);
        return result;
    }

    public Map<DataType, Map<String, Table>> getAllTables() {
        return allTables;
    }

}
