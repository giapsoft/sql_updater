package model;

import lombok.Data;
import mapper.Tracker;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class TrackerAction {
    boolean isCurrentExist = false;
    public boolean isDelete = false;

    String tableName;
    Map<String, String> pk;
    Map<String, String> columns = new TreeMap<>();

    public String[] toColumnArray(List<String> columns) {
        return columns.stream().map(this::getColumn).toArray(String[]::new);
    };

    public String getColumn(String name) {
        return pk.getOrDefault(name, columns.get(name));
    }

    public void isLastExisted(Tracker tracker) {
        isCurrentExist = !tracker.getIsDelete();
    }

    public static TrackerAction fromTracker(Tracker tracker) {
        TrackerAction action = new TrackerAction();
        action.pk = tracker.getPk();
        return action;
    }

    public static Map<String, TrackerAction> sumUp(List<Tracker> trackers) {
        Map<String, TrackerAction> result = new HashMap<>();
        trackers.forEach(t -> result.computeIfAbsent(t.getId(), k -> TrackerAction.fromTracker(t)).capture(t));
        result.keySet().forEach(k -> {
            if(result.get(k).isDelete) {
                result.remove(k);
            }
        });
        return result;
    }

    public void capture(Tracker tracker) {
        if (tracker.getIsDelete()) {
            columns.clear();
        } else {
            tracker.getColumns().forEach(columns::put);
        }
        isDelete = tracker.getIsDelete();
    }

    TrackerType getType() {
        if (isCurrentExist) {
            if (isDelete) {
                return TrackerType.delete;
            }
            return TrackerType.update;
        }
        if (isDelete) {
            return TrackerType.none;
        }
        return TrackerType.insert;

    }


    String findPk() {
        return pk.entrySet().stream().map(e -> String.format("%s = %s", e.getKey(), e.getValue())).collect(Collectors.joining(" AND "));
    }

    String setColumns() {
        return columns.entrySet().stream().map(e -> String.format("%s = %s", e.getKey(), e.getValue())).collect(Collectors.joining(", "));
    }

    String insertCols() {
        Set<String> cols = pk.keySet();
        cols.addAll(columns.keySet());
        return String.join(", ", cols);
    }

    String insertValues() {
        Collection<String> cols = pk.values();
        cols.addAll(columns.values());
        return String.join(", ", insertCols());
    }

    public String exportSql() {
        switch (getType()) {
            case insert:
                return String.format("INSERT INTO %s (%s) VALUES (%s);", tableName, insertCols(), insertValues());
            case delete:
                return String.format("DELETE FROM %S WHERE %s;", tableName, findPk());
            case update:
                return String.format("UPDATE %s SET %s WHERE %s;", tableName, setColumns(), findPk());
            default:
                return "";
        }
    }


}
