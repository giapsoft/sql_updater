package app;

import mapper.Table;
import mapper.Tracker;
import model.DataType;
import model.ListenType;
import util.StringUt;
import working.WorkingAction;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class AppState {
    private static String categoryName;

    public static String getCategoryName() {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            AppState.categoryName = "general";
        }
        return categoryName;
    }

    public static void setCategoryName(String categoryName) {
        setCategoryName(categoryName, ListenType.changed);
    }

    public static void setCategoryName(String categoryName, ListenType type) {
        AppState.categoryName = categoryName;
        setDataType(getDataType());
        categoryListeners.forEach(c -> c.accept(categoryName, type));
    }

    private static DataType dataType = DataType.common;

    public static DataType getDataType() {
        return AppState.dataType;
    }

    public static void setDataType(DataType type) {
        AppState.dataType = type;
        setTableName(null);
        dataTypeListeners.forEach(Runnable::run);
    }

    private static String tableName;

    public static void setTableName(String tableName, ListenType changeType) {
        AppState.tableName = StringUt.emptyTrim(tableName);
        tableListeners.forEach(l -> l.accept(tableName, changeType));
    }

    public static void setTableName(String tableName) {
        setTableName(tableName, ListenType.changed);
    }

    public static String getTableName() {
        return StringUt.emptyTrim(tableName);
    }

    public static Table currentTable() {
        return AppSummary.getTable(dataType, tableName);
    }

    public static Set<String> currentTableNames() {
        return AppSummary.tableNames(dataType);
    }

    public static List<Tracker> currentTableTrackers() {
        return Tracker.find(getCategoryName(), dataType, getTableName());
    }

    public static boolean existTable(String tableName) {
        return StringUt.anyContainsFirst(tableName, currentTableNames());
    }

    private static final List<BiConsumer<String, ListenType>> categoryListeners = new ArrayList<>();
    private static final List<Runnable> dataTypeListeners = new ArrayList<>();
    private static final List<BiConsumer<String, ListenType>> tableListeners = new ArrayList<>();
    private static final List<BiConsumer<Map<String, String>, ListenType>> rowListeners = new ArrayList<>();

    public static void listenCategory(BiConsumer<String, ListenType> listener) {
        categoryListeners.add(listener);
    }

    public static void listenDataType(Runnable listener) {
        dataTypeListeners.add(listener);
    }

    public static void listenTable(BiConsumer<String, ListenType> listener) {
        tableListeners.add(listener);
    }

    public static void listenRow(BiConsumer<Map<String, String>, ListenType> listener) {
        rowListeners.add(listener);
    }


    public static void addTable(Table table) {
        WorkingAction.get.addTable(getDataType(), table);
        tableListeners.forEach(l -> l.accept(table.getName(), ListenType.added));
        setTableName(table.getName());
    }

    public static void addCategory(String catName) {
        WorkingAction.get.addCategory(catName);
        categoryListeners.forEach(l -> l.accept(catName, ListenType.added));
        setCategoryName(catName);
    }

    public static void addRow(Map<String, String> pkColumns, Map<String, String> otherColumns) {
        WorkingAction.get.updateRow(AppState.getCategoryName(), AppState.getDataType(), AppState.getTableName(), pkColumns, otherColumns);
        Map<String, String> row = new HashMap<>();
        pkColumns.forEach(row::put);
        otherColumns.forEach(row::put);
        rowListeners.forEach(l -> l.accept(row, ListenType.added));
    }

    public static void deleteRow(Map<String, String> pkColumns) {
        WorkingAction.get.deleteRow(AppState.getCategoryName(), AppState.getDataType(), AppState.getTableName(), pkColumns);
        rowListeners.forEach(l -> l.accept(pkColumns, ListenType.removed));
    }

    public static void removeCategory(String catName) {
        WorkingAction.get.removeCategory(catName);
        categoryListeners.forEach(l -> l.accept(catName, ListenType.removed));
        setCategoryName("general");
    }

    public static void addColumn(String colName) {
        WorkingAction.get.addColumn(dataType, tableName, colName);
    }
}
