package app.common;

import app.models.Column;
import app.models.Row;
import app.models.Table;
import app.trackers.AppHistoryTracker;
import app.trackers.RowTracker;
import setup.Config;
import lombok.Data;
import util.*;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Data
public class Working implements Serializable {
    public static final Working get = new Working();
    HashMap<DataType, Map<String, Table>> addingTables = new HashMap<>();
    TreeMap<String, Set<String>> addingColumns = new TreeMap<>();
    HashSet<String> addingCategories = new HashSet<>();
    TreeMap<String, Row> updatingRows = new TreeMap<>();
    TreeSet<String> deletingRows = new TreeSet<>();

    CaptureInfo captureInfo;

    public int totalItems() {
        return addingTables.values().stream().mapToInt(s -> s.size()).sum()
                + addingColumns.size() + addingCategories.size()
                +updatingRows.size()
                +deletingRows.size();
    }

    public void init() {
        listenAppStates();
        initData();
    }



    void initData() {
        Working working = Encrypt.findJson(Working.class, Config.get.workingFilePath);
        if (working != null) {
            addingTables = new HashMap<>(working.addingTables);
            addingColumns = new TreeMap<>(working.addingColumns);
            addingCategories = new HashSet<>(working.addingCategories);
            updatingRows = new TreeMap<>(working.updatingRows);
            deletingRows = new TreeSet<>(working.deletingRows);
            captureInfo = working.captureInfo;
        }
    }

    private void listenAppStates() {
        AppState.get.listenAddCategory(this::addCategory);
        AppState.get.listenRevertAddCategory(this::removeCategory);
        AppState.get.listenAddTable(this::addTable);
        AppState.get.listenRevertAddTable(((dataType, tableName) -> {
            addingTables.getOrDefault(dataType, new HashMap<>()).remove(tableName);
        }));

        AppState.get.listenUpdateRow(this::updateRow);
        AppState.get.listenRevertUpdateRow(this::revertUpdateRow);
        AppState.get.listenDeleteRow(this::deleteRow);
        AppState.get.listenRevertDeleteRow(this::revertDeleteRow);
    }

    private void removeCategory(String catName) {
        addingCategories.remove(catName);
        save();
    }

    public void addColumn(Column column) {
        if (addingColumns.computeIfAbsent(column.getTableId(), c -> new HashSet<>())
                .add(column.getColumnName())) {
            save();
        }
    }

    public void updateRow(Row row) {
        updatingRows.put(row.getId(), row);
        save();
    }

    public void revertUpdateRow(String rowId) {
        updatingRows.remove(rowId);
        save();
    }

    public void deleteRow(String rowId) {
        deletingRows.add(rowId);
        save();
    }

    public void revertDeleteRow(String rowId) {
        deletingRows.remove(rowId);
        save();
    }

    public void save() {
        Encrypt.saveJson(get, Config.get.workingFilePath);
    }

    public boolean isAddingTable(Column column) {
        return addingTables.getOrDefault(column.getDataType(), new HashMap<>()).containsKey(column.getTableName());
    }

    public Set<String> findAddingColumns(Column column) {
        return addingColumns.getOrDefault(column.getTableId(), new HashSet<>());
    }

    public List<Column> findAddingColumnList(Column column) {
        return findAddingColumns(column).stream().map(column::createColumn).collect(Collectors.toList());
    }

    public void removeColumn(Column column) {
        if (addingColumns.getOrDefault(column.getTableId(), new HashSet<>()).remove(column.getColumnName())) {
            save();
        }
    }

    public Row findUpdatingRow(String id) {
        return updatingRows.get(id);
    }

    public boolean isUpdatingRow(String id) {
        return updatingRows.containsKey(id);
    }

    public boolean isDeletingRow(String id) {
        return deletingRows.contains(id);
    }

    public void addCategory(String name) {
        this.getAddingCategories().add(name);
        save();
    }

    public void addTable(Table table) {
        addingTables.computeIfAbsent(table.getDataType(), d -> new HashMap<>())
                .put(table.getName(), table);
        save();
    }

    public void commit(String commitMessage) throws Exception {
        Svn.update(Config.get.svnDir);
        doMerge();
        Svn.commit(Config.get.svnDir, commitMessage);
        reset();
    }

    public void doMerge() {
        mergeHistoryTracker();
        mergeRowTrackers();
    }

    public void reset() {
        addingTables.clear();
        addingColumns.clear();
        updatingRows.clear();
        deletingRows.clear();
        addingCategories.clear();
        save();
    }

    private void mergeRowTrackers() {
        Map<String, List<RowTracker>> pathTrackers = new HashMap<>();
        updatingRows.forEach((id, row) -> {
            String filePath = Config.get.tableFilePath(row.getCategoryName(), row.getDataType(), row.getTableName());
            RowTracker rowTracker = new RowTracker();
            rowTracker.setColumns(row.getColumns());
            pathTrackers.computeIfAbsent(filePath, f -> new ArrayList<>())
                    .add(rowTracker);
        });

        deletingRows.forEach(id -> {
            Row row = Row.decodeId(id);
            String tableFilePath = row.getTable().getFilePath(row.getCategoryName());
            RowTracker rowTracker = new RowTracker();
            rowTracker.setColumns(row.getColumns());
            rowTracker.setDelete(true);
            pathTrackers.computeIfAbsent(tableFilePath, f -> new ArrayList<>())
                    .add(rowTracker);
        });
        pathTrackers.forEach((path, trackers) -> {
            FileUt.appendToFile(path, Encrypt.encodeList(trackers));
        });
    }

    private void mergeHistoryTracker() {
        AppHistoryTracker historyTracker = new AppHistoryTracker();
        historyTracker.setAddingColumns(addingColumns);
        historyTracker.setAddingTables(addingTables);
        historyTracker.setAddingCategories(addingCategories);
        if (!historyTracker.isEmpty()) {
            FileUt.appendToFile(Config.get.appHistoryFilePath, Encrypt.encode(historyTracker));
        }

    }
}

