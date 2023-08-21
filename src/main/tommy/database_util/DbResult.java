package database_util;

import lombok.Data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Data
public class DbResult {
    public void addRow(ResultSet rs) {
        DbRow row = new DbRow();
        columns.forEach((name, column) -> {
            try {
                row.db = this;
                row.cells.put(name, column.get(rs));
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        rows.add(row);
    }

    List<DbRow> rows = new ArrayList<>();
    Map<String, DbColumn> columns = new HashMap<>();


}
