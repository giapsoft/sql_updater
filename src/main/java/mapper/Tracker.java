package mapper;

import lombok.Data;
import model.DataType;
import service.Config;
import util.Finder;
import util.StringUt;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class Tracker implements Serializable {
    Map<String, String> pk = new HashMap<>();
    Map<String, String> columns = new HashMap<>();
    Boolean isDelete = false;

    public String getId() {
        return StringUt.toId(pk);
    }

    public Tracker with(Map<String, String> pk, Map<String, String> columns, boolean isDelete) {
        this.pk = pk;
        this.columns = columns;
        this.isDelete = isDelete;
        return this;
    }

    public static Map<String, List<Tracker>> trackers = new HashMap<>();

    public static List<Tracker> find(String categoryName, DataType type, String tableName) {
        String fullPath = getPath(categoryName, type, tableName);
        return trackers.computeIfAbsent(fullPath, f -> Finder.findAll(Tracker.class, fullPath));
    }

    public static String getPath(String categoryName, DataType type, String tableName) {
        return String.join("\\", Config.svnDir, categoryName, type.name(), tableName + ".store");
    }
}
