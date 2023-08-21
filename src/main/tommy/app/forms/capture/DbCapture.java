package app.forms.capture;

import app.common.DataType;
import app.common.Working;
import app.models.Row;
import app.models.Table;
import database_util.DatabaseRunner;
import database_util.DbResult;
import database_util.DbRow;

import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DbCapture {
    final DatabaseRunner db;
    final DataType dataType;
    final String category;

    public DbCapture(String category, DatabaseRunner db, DataType dataType) {
        this.db = db;
        this.dataType = dataType;
        this.category = category;
    }

    List<Table> tables = new ArrayList<>();
    Map<String, List<Row>> rows = new HashMap<>();


    public void mergeToWorking() {
        int countTable = 0;
        for (Table table : tables) {
            countTable++;
            Working.get.addTable(table);
            int count = 0;
            List<Row> rowList = rows.get(table.getName());
            notification.accept(String.format("merging table %s (%s/%s)", table.getName(), countTable, tables.size()));
            for (Row row : rowList) {
                count++;
                notification.accept(String.format("merging table %s (%s/%s), row %s/%s...", table.getName(), countTable, tables.size(), count, rowList.size()));
                Working.get.updateRow(row);
            }
        }
        notification.accept("Merged successfully.");
    }

    Consumer<String> notification;

    List<String> tableNames;
    public void capture(Consumer<String> notification) {
        this.notification = notification;
        notification.accept("start");
        tables.clear();
        rows.clear();
        db.start();
        try {
            tableNames = findAllTableNames();
            for (String tableName : tableNames) {
                captureTable(tableName);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            notification.accept(ex.getMessage());
        } finally {
            try {
                notification.accept("scan ok, waiting merge...");
                db.finish();
            } catch (SQLException ex1) {
                ex1.printStackTrace();
                notification.accept(ex1.getMessage());
            }
        }
    }

    private void captureTable(String tableName) throws SQLException {
        Set<String> pkColumns = pkColumns(tableName);
        Set<String> otherColumns = allColumns(tableName);
        otherColumns.removeAll(pkColumns);
        Table table = new Table();
        table.setName(tableName);
        table.setOtherColumns(otherColumns);
        table.setDataType(dataType);
        table.setPkColumns(pkColumns);
        tables.add(table);
        captureRows(table);
    }

    private void captureRows(Table table) throws SQLException {
        String sql = String.format("Select * from %s", table.getName());
        DbResult result = db.select(sql);
        List<Row> tableRows = new ArrayList<>();
        if (!result.getColumns().isEmpty() && !result.getRows().isEmpty()) {
            int rowCount = 0;
            for (DbRow dbRow : result.getRows()) {
                Row row = new Row(category, dataType, table.getName(), new HashMap<>());
                for (String columnName : dbRow.getCells().keySet()) {
                    row.getColumns().put(columnName, dbRow.getAsString(columnName));
                }
                row.setTable(table);
                tableRows.add(row);
                notification.accept(String.format("Progress: %s/%s, table: %s, row: %s", tables.size(), tableNames.size(), table.getName(), ++rowCount));
            }
        }
        rows.put(table.getName(), tableRows);
    }

    private Set<String> allColumns(String tableName) throws SQLException {
        String sql = String.format("Select * from %s where false", tableName);
        DbResult dbResult = db.select(sql);
        return dbResult.getColumns().keySet();
    }


    private Set<String> pkColumns(String tableName) throws SQLException {
        String sql = String.format("SELECT pg_attribute.attname " +
                "FROM pg_index, pg_class, pg_attribute, pg_namespace \n" +
                "WHERE \n" +
                "  pg_class.oid = '%s'::regclass AND \n" +
                "  indrelid = pg_class.oid AND \n" +
                "  nspname = 'public' AND \n" +
                "  pg_class.relnamespace = pg_namespace.oid AND \n" +
                "  pg_attribute.attrelid = pg_class.oid AND \n" +
                "  pg_attribute.attnum = any(pg_index.indkey)\n" +
                " AND indisprimary", tableName);
        DbResult dbResult = db.select(sql);
        for (DbRow row : dbResult.getRows()) {
            row.getAsString("attname");
        }
        return dbResult.getRows().stream().map(r -> r.getAsString("attname")).collect(Collectors.toSet());
    }

    private List<String> findAllTableNames() throws SQLException {
        DbResult dbResult = db.select("SELECT * FROM information_schema.tables where table_type = 'BASE TABLE' and table_schema = 'public'");
        return dbResult.getRows().stream()
                .map(r -> r.getAsString("table_name"))
                .collect(Collectors.toList());
    }
}
