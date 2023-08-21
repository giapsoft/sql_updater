package working;

import app.AppData;
import lombok.Data;
import mapper.Table;
import mapper.Tracker;
import model.DataType;
import service.Config;
import util.FileUt;
import util.Finder;
import util.ObjectUt;
import util.StringUt;

import java.io.Serializable;
import java.util.*;

@Data
public class WorkingAction implements Serializable {
    private static List<Runnable> listeners = new ArrayList<>();
    public static WorkingAction get = ObjectUt.firstNonNull(Finder.find(WorkingAction.class, Config.workingFilePath), new WorkingAction());
    public void listen(Runnable listener) {
        listeners.add(listener);
    }

    public static void save() {
        Finder.save(get, Config.workingFilePath);
        listeners.forEach(Runnable::run);
    }

    // datatype <tableName, <[pk|others], names>>
    HashMap<DataType, Map<String, Map<String, Set<String>>>> addingTables = new HashMap<>();
    HashMap<DataType, Map<String, Set<String>>> addingColumns = new HashMap<>();
    HashSet<String> addingCategories = new HashSet<>();

    // category,        <datatype, <tableName,   <rowId,      <[pk | others], <name, value>>>>>
    HashMap<String, Map<DataType, Map<String, Map<String, Map<String, Map<String, String>>>>>> updatingRows = new HashMap<>();
    // category,       <datatype, <tableName,    <rowId,  <pkName, pkValue>>>>
    HashMap<String, Map<DataType, Map<String, Map<String, Map<String, String>>>>> deletingRows = new HashMap<>();

    public boolean addColumn(DataType dataType, String tableName, String columnName) {
        if(addingColumns.computeIfAbsent(dataType, d -> new HashMap<>())
                .computeIfAbsent(tableName, t -> new HashSet<>())
        .add(columnName)) {
            save();
            return true;
        }
        return false;
    }

    // [pk | others]
    public Map<String, Set<String>> getTableCols(DataType dataType, String tableName) {
        Map<String, Set<String>> table = addingTables.getOrDefault(dataType, new HashMap<>()).getOrDefault(tableName, new HashMap<>());
        Set<String> others = new HashSet<>(table.getOrDefault("others", new HashSet<>()));
        others.addAll(addingColumns.getOrDefault(dataType, new HashMap<>()).getOrDefault(tableName, new HashSet<>()));
        if (!others.isEmpty()) {
            table.put("others", others);
        }
        return table;
    }

    public Set<String> getTableNames(DataType type) {
        return addingTables.getOrDefault(type, new HashMap<>()).keySet();
    }

    public void addCategory(String name) {
        if (!addingCategories.contains(name)) {
            addingCategories.add(name);
            save();
        }
    }

    public void removeCategory(String name) {
        if (addingCategories.contains(name)) {
            addingCategories.remove(name);
            updatingRows.remove(name);
            save();
        }
    }

    public void addTable(DataType dataType, Table table) {
        String tableName = table.getName();

        Map<String, Set<String>> rawTable = addingTables.computeIfAbsent(dataType, d -> new HashMap<>())
                .computeIfAbsent(tableName, t -> new HashMap<>());
        rawTable.put("pk", table.getPkColumns());
        rawTable.put("others", table.getOtherColumns());
        save();
    }

    public void updateTable(DataType dataType, String tableName, Set<String> others) {
        Map<String, Set<String>> table = addingColumns.computeIfAbsent(dataType, d -> new HashMap<>());
        Set<String> addingCols = new HashSet<>(table.computeIfAbsent(tableName, t -> new HashSet<>()));
        addingCols.addAll(others);
        table.put(tableName, addingCols);
        save();
    }

    public void updateRow(String category, DataType dataType, String tableName, Map<String, String> pk, Map<String, String> data) {
        String rowId = StringUt.toId(pk);
        Map<String, Map<String, String>> currentUpdating = updatingRows.computeIfAbsent(category, c -> new HashMap<>())
                .computeIfAbsent(dataType, t -> new HashMap<>())
                .computeIfAbsent(tableName, tb -> new HashMap<>())
                .computeIfAbsent(rowId, r -> new HashMap<>());
        Map<String, String> currentPk = currentUpdating.computeIfAbsent("pk", r -> new HashMap<>());
        pk.forEach(currentPk::put);

        Map<String, String> currentOthers = currentUpdating.computeIfAbsent("others", r -> new HashMap<>());
        data.forEach(currentOthers::put);
        save();
    }

    public void deleteRow(String category, DataType dataType, String tableName, Map<String, String> pk) {
        String rowId = StringUt.toId(pk);
        Map<String, String> currentUpdating = deletingRows.computeIfAbsent(category, c -> new HashMap<>())
                .computeIfAbsent(dataType, t -> new HashMap<>())
                .computeIfAbsent(tableName, tb -> new HashMap<>())
                .computeIfAbsent(rowId, r -> new HashMap<>());
        pk.forEach(currentUpdating::put);
        save();
    }


    public static Map<String, Object> waitingCommit = new HashMap<>();

    public void commit() {
        waitingCommit.clear();
        commitCommonData();
        commitRows();
        waitingCommit.forEach(FileUt::writeJsonObject);
    }


    public void commitCommonData() {
        if (!addingCategories.isEmpty()) {
            AppData.get.getCategories().addAll(addingCategories);
        }
        if (!addingTables.isEmpty()) {
            addingTables.forEach((type, map) -> {
                Map<String, Table> tables = AppData.get.getTables(type);
                map.forEach((tableName, columns) -> {
                    if (tables.get(tableName) == null) {
                        tables.put(tableName, new Table(tableName, columns.get("pk"), columns.get("others")));
                    }
                });
            });
        }
        if (!addingColumns.isEmpty()) {
            addingColumns.forEach((type, map) -> {
                Map<String, Table> tables = AppData.get.getTables(type);
                map.forEach((tableName, columns) -> {
                    Table table = tables.get(tableName);
                    if (table != null) {
                        table.getOtherColumns().addAll(columns);
                    }
                });
            });
        }
        waitingCommit.put(Config.svnAppData, AppData.get);
    }

    public void commitRows() {
        updatingRows.forEach((cat, typeMapMap) -> {
            typeMapMap.forEach((dataType, tableMap) -> {
                tableMap.forEach((tableName, idMap) -> {
                    List<Tracker> currents = Tracker.find(cat, dataType, tableName);
                    idMap.forEach((id, colGroups) -> {
                        Map<String, String> pk = colGroups.get("pk");
                        Map<String, String> others = colGroups.getOrDefault("others", new HashMap<>());
                        currents.add(new Tracker().with(pk, others, false));
                        waitingCommit.put(Tracker.getPath(cat, dataType, tableName), currents);
                    });
                });
            });
        });

        deletingRows.forEach((cat, typeMapMap) -> {
            typeMapMap.forEach((dataType, tableMap) -> {
                tableMap.forEach((tableName, idMap) -> {
                    List<Tracker> currents = Tracker.find(cat, dataType, tableName);
                    idMap.forEach((id, pk) -> {
                        currents.add(new Tracker().with(pk, new HashMap<>(), true));
                        waitingCommit.put(Tracker.getPath(cat, dataType, tableName), currents);
                    });
                });
            });
        });
    }
}
