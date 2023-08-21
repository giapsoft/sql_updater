package app.trackers;

import lombok.Data;
import util.Encrypt;

import java.util.List;
import java.util.Map;

@Data
public class RowTracker {
    Map<String, String> columns;
    boolean isDelete = false;

    public static List<RowTracker> parse(String filePath) {
        return Encrypt.findAll(RowTracker.class, filePath);
    }

    public static List<RowTracker> parseFromRaw(String raw) {
        return Encrypt.decode(raw, RowTracker.class);
    }
}
