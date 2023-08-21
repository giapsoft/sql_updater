package database_util;

import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DbRow {
    DbResult db;
    Map<String, Object> cells = new HashMap<>();
    public String getAsString(String name) {
        int type = db.columns.get(name).type;
        Object value = cells.get(name);
        if (value == null) {
            return null;
        }
        if (Arrays.asList(Types.BLOB, Types.CLOB, Types.NCLOB).contains(type)) {
            return null;
        }
        if(Types.TIMESTAMP == type) {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Date) value);
        }
        if (Types.DATE == type) {
            java.sql.Date date = (java.sql.Date) value;
            return new SimpleDateFormat("yyyy-MM-dd").format(date);
        }
        if (Types.TIME == type) {
            return new SimpleDateFormat("hh:mm:ss").format((Date) value);
        }
        return value.toString();
    }

    public Map<String, Object> getCells() {
        return cells;
    }
}
