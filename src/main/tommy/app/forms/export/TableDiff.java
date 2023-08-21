package app.forms.export;

import app.common.AppState;
import app.common.DataType;
import app.models.Row;
import app.models.Table;
import database_util.DatabaseRunner;
import lombok.Data;
import setup.Config;
import util.FileUt;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class TableDiff {
    String filePath;
    String oldContent = "";
    String appendContent = "";
    Map<String, Row> rowsBefore;
    Map<String, Row> rowsAfter;
    List<Row> finalRows;
    Table table;

    @Override
    public String toString() {
        return String.format("filePath: %s\n\tOld content: %s\n\tAppend content: %s", filePath, oldContent, appendContent);
    }

    public String toSqlString() {
        if (finalRows != null && !finalRows.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            builder.append("\n").append(table.getId());
            finalRows.forEach(r -> builder.append("\n\t").append(r.toSql()));
            return builder.toString();
        }
        return "";
    }

    void load() {
        if (Config.get.isNotTableFilePath(filePath)) {
            return;
        }
        String[] parts = filePath.split("[^a-z_.]");
        String tableName = parts[parts.length - 1].split(".store")[0];
        DataType dataType = DataType.valueOf(parts[parts.length - 2]);
        String categoryName = parts[parts.length - 3];
        table = AppState.get.data.getTable(dataType, tableName);
        if (table == null) {
            return;
        }
        rowsBefore = table.loadRows(categoryName, oldContent);
        rowsAfter = table.loadRows(categoryName, appendContent);
        finalRows = new ArrayList<>();
        rowsAfter.forEach((id, rowAfter) -> {
            Row rowBefore = rowsBefore.get(id);
            if (rowBefore != null) {
                if (rowBefore.isDeleted() && rowAfter.isDeleted()) {
                    return;
                }
                if (rowAfter.isDeleted()) {
                    rowAfter.setSqlAction(Row.SqlAction.delete);
                    finalRows.add(rowAfter);
                    return;
                }

                if (rowBefore.isDeleted()) {
                    rowAfter.setSqlAction(Row.SqlAction.insert);
                    finalRows.add(rowAfter);
                    return;
                }

                rowAfter.setSqlAction(Row.SqlAction.update);
                finalRows.add(rowAfter);

            } else {
                if (!rowAfter.isDeleted()) {
                    rowAfter.setSqlAction(Row.SqlAction.insert);
                    finalRows.add(rowAfter);
                }
            }
        });
    }

    public final List<String> errors = new ArrayList<>();
    public void run(DatabaseRunner databaseRunner) {
        for (Row finalRow : finalRows) {
            String sql = finalRow.toSql();
            try {
                databaseRunner.update(sql);
            } catch (Exception ex) {
                errors.add(String.format("Error %s on statement: %s", ex.getMessage(), sql));
            }
        }
        if (!errors.isEmpty()) {
            String errorString = String.format("\r\n%s - %s: \r\n\t%s",
                    table.getId(),
                    new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()),
                    String.join("\n\r\t", errors));
            FileUt.appendToFile(Config.get.updateErrorsFilePath, errorString);
        }
    }
}
