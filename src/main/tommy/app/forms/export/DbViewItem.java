package app.forms.export;

import app.common.AppBuilder;
import app.common.AppConfig;
import app.common.DataType;
import app.common.TextClass;
import app.forms.export.add_db.AddDb;
import app.icons.Icons;
import database_util.DatabaseRunner;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.tmatesoft.svn.core.SVNException;
import setup.Config;
import util.Encrypt;
import util.StringUt;
import util.Svn;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DbViewItem {
    static Map<String, DbViewItem> itemMap = new HashMap<>();
    static Map<String, Map<DataType, List<TableDiff>>> diffMap = new HashMap<>();

    static void reset() {
        itemMap.clear();
        diffMap.clear();
    }

    public static DbViewItem of(String id) {
        return itemMap.computeIfAbsent(id, i -> {
            DbInfo db = DbHolder.get.findDb(id);
            return new DbViewItem(db);
        });
    }

    DbInfo db;
    Button editBtn;
    CheckBox checkBox;
    Button deleteBtn;
    HBox container;
    Label status;
    boolean isSelected;
    Label latestRev;
    Label dataType;

    DbViewItem(DbInfo db) {
        this.db = db;
        editBtn = Icons.edit.button();
        editBtn.setOnMouseClicked(l -> {
            AddDb.currentDbInfo = db;
            AppBuilder.showDialog(AddDb.class);
        });
        checkBox = new CheckBox(db.url);
        checkBox.setId(db.id);
        deleteBtn = Icons.delete.button();
        checkBox.selectedProperty().addListener(l -> {
            this.isSelected = checkBox.isSelected();
        });
        deleteBtn.setOnMouseClicked(l -> DbHolder.get.removeDb(db));
        status = new Label();
        dataType = new Label();
        latestRev = new Label();
        dataType.setPrefWidth(60);
        updateView();
        container = AppBuilder.hBox(dataType, checkBox, latestRev, editBtn, deleteBtn, status);
        databaseRunner = new DatabaseRunner(db.url, db.user, db.password);
    }

    public void setSelected(boolean value) {
        checkBox.setSelected(value);
    }

    public void setDisable(boolean value) {
        deleteBtn.setDisable(value);
        editBtn.setDisable(value);
        checkBox.setDisable(value);
    }

    public void update(long toRev, Runnable onDone) {
        setStatus("starting...");
        new Thread(() -> {
            try {
                doUpdate(toRev);
            } catch (Exception e) {
                e.printStackTrace();
                setStatus(e.getMessage());
            } finally {
                onDone.run();
            }
        }).start();
    }

    DatabaseRunner databaseRunner;

    private void doUpdate(long rev) throws SQLException {
        Connection connection = databaseRunner.start();
        if (connection == null) {
            setStatus(TextClass.error, "cannot connect!");
        } else {
            exportByRevision(db.lastRevision, rev);
            databaseRunner.finish();
            db.setLastRevision(rev);
            DbHolder.get.save();
            if (hasError) {
                setStatus(TextClass.error, "error!");
            } else {
                setStatus(TextClass.success, "success!");
            }
        }
    }

    public void setStatus(String text) {
        setStatus(TextClass.help, text);
    }

    public void setStatus(TextClass type, String text) {
        Platform.runLater(() -> {
            status.getStyleClass().clear();
            type.set(status);
            status.setText(text);
        });
    }

    public void updateView() {
        latestRev.setText("(" + db.getLastRevision().toString() + ")");
        dataType.setText(db.isCtrl ? "CTRL" : "MAIN");
        checkBox.setText(db.url);
        dataType.getStyleClass().clear();
        dataType.getStyleClass().add(db.isCtrl ? "text-ctrl" : "text-main");
    }

    boolean hasError =false;
    public void exportByRevision(long from, long to) {
        from = Math.max(from, AppConfig.get().getMinimumRevision());
        hasError = false;
        List<TableDiff> items = loadDiff(from, to).get(db.isCtrl ? DataType.ctrl : DataType.main);
        if (items == null) {
            return;
        }
        for (TableDiff item : items) {
            item.run(databaseRunner);
            if(!item.errors.isEmpty()) {
                hasError = true;
            }
        }
    }

    static Map<DataType, List<TableDiff>> loadDiff(long from, long to) {
        return diffMap.computeIfAbsent(String.format("%s||%s", from, to), d -> {
            try {
                String diff = Svn.diff(Config.get.svnDir, from, to);
                List<TableDiff> items = new ArrayList<>();
                for (String line : diff.split("\n")) {
                    TableDiff tableDiff = new TableDiff();
                    if (line.startsWith("Index: ")) {
                        tableDiff = new TableDiff();
                        tableDiff.setFilePath(StringUt.emptyTrim(line.substring("Index: ".length())));
                        items.add(tableDiff);
                    }
                    if (line.startsWith("-" + Encrypt.joiner)) {
                        tableDiff.setOldContent(line.substring(1));
                    }

                    if (line.startsWith("+" + Encrypt.joiner)) {
                        int oldLen = tableDiff.getOldContent().length();
                        int subIdx = oldLen == 0 ? 1 : oldLen;
                        tableDiff.setAppendContent(line.substring(subIdx));
                    }
                    tableDiff.load();
                }
                return items.stream().filter(item -> item.table != null && item.finalRows != null && !item.finalRows.isEmpty())
                        .collect(Collectors.groupingBy(item -> item.table.getDataType()));
            } catch (SVNException e) {
                e.printStackTrace();
                return new HashMap<>();
            }
        });
    }
}
