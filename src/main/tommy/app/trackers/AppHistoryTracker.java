package app.trackers;

import app.common.DataType;
import app.models.Table;
import lombok.Data;

import java.util.*;

@Data
public class AppHistoryTracker {
    HashMap<DataType, Map<String, Table>> addingTables = new HashMap<>();
    TreeMap<String, Set<String>> addingColumns = new TreeMap<>();
    HashSet<String> addingCategories = new HashSet<>();
    public boolean isEmpty() {
        return addingCategories.isEmpty() && addingColumns.isEmpty() && addingTables.isEmpty();
    }
}
