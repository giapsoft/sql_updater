package app.common;

import app.models.Column;
import app.models.Row;
import app.models.Table;
import lombok.Getter;
import lombok.Setter;
import util.StringUt;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class AppState {
    public static String generalCat = "general";

    @Getter
    @Setter
    public static class Selection {
        String catName;
        DataType dataType = DataType.ctrl;
        Table table;
        RowAction rowAction;
        Row updatingRow;
    }

    @Getter
    @Setter
    public static class Data {
        public void reset() {
            catNames.clear();
            tables.clear();
            tableNames.clear();
        }
        TreeSet<String> catNames = new TreeSet<>();
        Map<DataType, Map<String, Table>> tables = new HashMap<>();
        Map<DataType, List<String>> tableNames = new HashMap<>();

        public List<String> findTableNames(DataType dataType, String searchKey) {
            Supplier<List<String>> getTableNames = () -> {
                List<String> names = new ArrayList<>(tables.getOrDefault(dataType, new HashMap<>()).keySet());
                names.sort(Comparator.comparing(c -> c));
                return names;
            };
            List<String> all = getTableNames.get();
            return StringUt.isEmptyTrim(searchKey) ? all : all.stream().filter(c -> StringUt.isSearchContains(c, searchKey)).collect(Collectors.toList());
        }

        public Table getTable(DataType dataType, String name) {
            return tables.getOrDefault(dataType, new HashMap<>()).get(name);
        }

        void addColumn(Column column) {
            column.getTable().getOtherColumns().add(column.getColumnName());
        }

        public void removeColumn(Column column) {
            column.getTable().getOtherColumns().remove(column.getColumnName());
        }

        void addCategory(String name) {
            getCatNames().add(name);
        }

        public void addTable(Table table) {
            tableNames.computeIfAbsent(table.getDataType(), d -> new ArrayList<>()).add(table.getName());
            tables.computeIfAbsent(table.getDataType(), d -> new HashMap<>())
                    .put(table.getName(), table);
        }

        public void removeTable(DataType dataType, String tableName) {
            tableNames.computeIfAbsent(dataType, d -> new ArrayList<>()).remove(tableName);
            tables.computeIfAbsent(dataType, d -> new HashMap<>()).remove(tableName);
        }

        public void removeCategory(String name) {
            getCatNames().remove(name);
        }
    }

    public final Selection selection = new Selection();
    public final Data data = new Data();

    public static final AppState get = new AppState();

    List<Consumer<String>> addCategoryListeners = new ArrayList<>();
    List<Consumer<String>> selectCategoryListeners = new ArrayList<>();
    List<Consumer<String>> revertAddCategoryListeners = new ArrayList<>();
    List<Consumer<DataType>> selectDataTypeListeners = new ArrayList<>();
    List<BiConsumer<DataType, Table>> selectTableListeners = new ArrayList<>();
    List<Consumer<Table>> addTableListeners = new ArrayList<>();
    List<BiConsumer<DataType, String>> revertAddTableListeners = new ArrayList<>();
    List<Consumer<Column>> addColumnListeners = new ArrayList<>();
    List<Consumer<Row>> updateRowListeners = new ArrayList<>();
    List<Consumer<String>> revertUpdateRowListeners = new ArrayList<>();
    List<Consumer<String>> deleteRowListeners = new ArrayList<>();
    List<Consumer<String>> revertDeleteRowListeners = new ArrayList<>();

    public void listenUpdateRow(Consumer<Row> listener) {
        updateRowListeners.add(listener);
    }

    public void listenRevertUpdateRow(Consumer<String> listener) {
        revertUpdateRowListeners.add(listener);
    }

    public void listenDeleteRow(Consumer<String> listener) {
        deleteRowListeners.add(listener);
    }

    public void listenRevertDeleteRow(Consumer<String> listener) {
        revertDeleteRowListeners.add(listener);
    }

    public void updateRow(Row row) {
        updateRowListeners.forEach(u -> u.accept(row));
    }

    public void revertUpdateRow(String rowId) {
        revertUpdateRowListeners.forEach(u -> u.accept(rowId));
    }

    public void deleteRow(String rowId) {
        deleteRowListeners.forEach(d -> d.accept(rowId));
    }

    public void revertDeleteRow(String rowId) {
        revertDeleteRowListeners.forEach(r -> r.accept(rowId));
    }

    public void addColumn(Column column) {
        data.addColumn(column);
        Working.get.addColumn(column);
        addColumnListeners.forEach(c -> c.accept(column));
    }

    public void removeColumn(Column column) {
        data.removeColumn(column);
        Working.get.removeColumn(column);
    }

    public void removeAllColumns(Column column) {
        Working.get.findAddingColumnList(column).forEach(AppState.get::removeColumn);
    }

    public void listenAddColumn(Consumer<Column> listener) {
        addColumnListeners.add(listener);
    }

    public void selectCategory(String catName) {
        selection.setCatName(catName);
        selectTable(null);
        selectCategoryListeners.forEach(c -> c.accept(catName));
    }

    public void listenSelectDataType(Consumer<DataType> listener) {
        selectDataTypeListeners.add(listener);
    }

    public void listenSelectCategory(Consumer<String> listener) {
        selectCategoryListeners.add(listener);
    }

    public void listenAddCategory(Consumer<String> listener) {
        addCategoryListeners.add(listener);
    }

    public void listenSelectTable(BiConsumer<DataType, Table> listener) {
        selectTableListeners.add(listener);
    }

    public void listenAddTable(Consumer<Table> listener) {
        addTableListeners.add(listener);
    }

    public void listenRevertAddCategory(Consumer<String> listener) {
        revertAddCategoryListeners.add(listener);
    }

    public void listenRevertAddTable(BiConsumer<DataType, String> listener) {
        revertAddTableListeners.add(listener);
    }

    public void addCategory(String name) {
        data.addCategory(name);
        Working.get.addCategory(name);
        selectCategory(name);
        addCategoryListeners.forEach(a -> a.accept(name));
    }

    public void revertAddCategory(String name) {
        data.removeCategory(name);
        selectCategory(generalCat);
        revertAddCategoryListeners.forEach(listener -> listener.accept(name));
    }

    public void selectDataType(DataType type) {
        selection.setDataType(type);
        selectDataTypeListeners.forEach(l -> l.accept(type));
        selectTable(null);
    }

    public void selectTableByName(String name) {
        Table table = data.getTable(selection.dataType, name);
        selectTable(table);
    }

    public void selectTable(Table table) {
        selection.setTable(table);
        selectTableListeners.forEach(t -> t.accept(selection.dataType, table));
    }

    public void createTable(String tableName, Set<String> pkColumns, Set<String> otherColumns) {
        Table table = new Table(selection.dataType, tableName, pkColumns, otherColumns);
        addTable(table);
    }

    public void addTable(Table table) {
        data.addTable(table);
        addTableListeners.forEach(a -> a.accept(table));
    }

    public void revertAddTable(DataType dataType, String tableName) {
        data.removeTable(dataType, tableName);
        selectTable(null);
        revertAddTableListeners.forEach(a -> a.accept(dataType, tableName));
    }

    public void init() {
        AppHistory.get.init();
        Working.get.init();
        data.catNames = new TreeSet<>(AppHistory.get.allCatNames());
        data.catNames.addAll(Working.get.addingCategories);
        data.tables = new HashMap<>(AppHistory.get.getAllTables());
        Working.get.addingTables.forEach((dataType, tableMap)
                -> tableMap.forEach((id, table)
                -> data.tables.computeIfAbsent(dataType, d -> new HashMap<>())
                .put(id, table)));
        data.catNames.add(generalCat);
        selection.setCatName(generalCat);
    }

    public void reset() throws IOException {
        data.reset();
        AppHistory.get.reset();
        Working.get.reset();
    }
}
