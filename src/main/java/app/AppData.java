package app;

import lombok.Data;
import mapper.Table;
import model.DataType;
import service.Config;
import util.Finder;
import util.ObjectUt;
import working.WorkingAction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class AppData implements Serializable {
    Map<String, Table> ctrlTables = new HashMap<>();
    Map<String, Table> mainTables = new HashMap<>();
    Map<String, Table> commonTables = new HashMap<>();

    public Map<String, Table> getTables(DataType dataType) {
        switch (dataType) {
            case main: return mainTables;
            case common: return commonTables;
            default: return ctrlTables;
        }
    }

    public Table getTable(DataType dataType, String tableName) {
        return getTables(dataType).get(tableName);
    }

    private List<String> categories = new ArrayList<>();

    public List<String> getCategories() {
        if (categories.isEmpty()) {
            WorkingAction.get.addCategory("general");
        }
        return categories;
    }

    public static AppData get = ObjectUt.firstNonNull(Finder.find(AppData.class, Config.svnAppData), new AppData());

}
