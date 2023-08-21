package app.forms.export;

import app.common.ActionType;
import setup.Config;
import util.Encrypt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class DbHolder {
    Map<String, DbInfo> dbMap = new HashMap<>();

    public static DbHolder get;

    public static void init() {
        if (get == null) {
            get = Encrypt.findJson(DbHolder.class, Config.get.databasesPath);
            if (get == null) {
                get = new DbHolder();
            }
        }

    }

    static Map<String, BiConsumer<DbInfo, ActionType>> dbListeners = new HashMap<>();

    static void listen(String name, BiConsumer<DbInfo, ActionType> listener) {
        dbListeners.put(name, listener);
    }

    public void save() {
        Encrypt.saveJson(this, Config.get.databasesPath);
    }

    public void addDb(DbInfo db) {
        if (!dbMap.containsKey(db.id)) {
            dbMap.put(db.id, db);
            dbListeners.values().forEach(d -> d.accept(db, ActionType.add));
            save();
        }
    }

    public void removeDb(DbInfo db) {
        dbMap.remove(db.id);
        dbListeners.values().forEach(d -> d.accept(db, ActionType.delete));
        save();
    }

    public void updateDb(DbInfo db) {
        DbInfo getDb = dbMap.get(db.id);
        if (getDb != null) {
            save();
            dbListeners.values().forEach(d -> d.accept(db, ActionType.update));
        }
    }

    public DbInfo findDb(String id) {
        return dbMap.get(id);
    }
}
