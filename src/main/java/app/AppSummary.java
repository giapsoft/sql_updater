package app;

import mapper.Table;
import mapper.Tracker;
import model.DataType;
import model.TrackerAction;
import working.WorkingAction;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AppSummary {
    public static Table getTable(DataType dataType, String tableName) {
        Map<String, Set<String>> tblCols = WorkingAction.get.getTableCols(dataType, tableName);
        Table table = AppData.get.getTable(dataType, tableName);
        if (table == null) {
            if(tblCols.isEmpty()) {
                return null;
            }
            table = new Table(tableName, tblCols.get("pk"), tblCols.get("others"));
        } else {
            table.addColumns(tblCols.get("others"));
        }
        return table;
    }

    public static Set<String> tableNames(DataType type) {
        Set<String> fromApp = new HashSet<>(AppData.get.getTables(type).keySet());
        Set<String> fromWorking = WorkingAction.get.getTableNames(type);
        fromApp.addAll(fromWorking);
        return fromApp;
    }

    public static Set<String> allCategories() {
        Set<String> result = new HashSet<>(AppData.get.getCategories());
        result.addAll(WorkingAction.get.getAddingCategories());
        return result;
    }
}
