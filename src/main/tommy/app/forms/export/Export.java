package app.forms.export;

import app.common.ActionType;
import app.common.AppBuilder;
import app.forms.export.add_db.AddDb;
import app.forms.export.select_rev.SelectRev;
import app.icons.Icons;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import setup.Config;
import util.FileUt;
import util.Svn;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class Export extends AppBuilder {

    @FXML
    ListView<HBox> listViewDatabases;
    @FXML
    Button buttonAddDb;
    @FXML
    Label labelStatus;
    @FXML
    Label labelLastRev;

    @FXML
    CheckBox checkBoxSelectAll;

    @FXML
    Button btnSelectRev;

    @FXML
    Button btnUpdateToLatest;
    @FXML
    Button btnUpdateToRev;
    @FXML
    TextField textFieldRev;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        DbViewItem.reset();
        buttonViewErrorsFile.setVisible(false);
        btnUpdateToLatest.setDisable(true);
        labelStatus.setText("");
        DbHolder.listen("DbView", ((dbInfo, actionType) -> {
            if (actionType == ActionType.add) {
                addDbView(dbInfo);
            } else if (actionType == ActionType.delete) {
                removeDbView(dbInfo);
            } else if (actionType == ActionType.update) {
                updateDbView(dbInfo);
            }
        }));
        Icons.add.set(buttonAddDb);
        Icons.search.set(btnSelectRev);
        for (DbInfo dbInfo : DbHolder.get.dbMap.values()) {
            addDbView(dbInfo);
        }

        checkBoxSelectAll.selectedProperty().addListener(l -> {
            DbViewItem.itemMap.values().forEach(v -> v.setSelected(checkBoxSelectAll.isSelected()));
        });
        updateLatestRevisions();
    }

    void updateLatestRevisions() {
        new Thread(() -> {
            Long lastRev = null;
            try {
                lastRev = Svn.lastRev();
            } catch (SVNException e) {
                e.printStackTrace();
            }
            final Long lastRevFn = lastRev == null ? 0 : lastRev;
            Platform.runLater(() -> {
                labelLastRev.setText(String.valueOf(lastRevFn));
                btnUpdateToLatest.setDisable(false);
            });
        }).start();
    }

    private void updateDbView(DbInfo dbInfo) {
        DbViewItem.of(dbInfo.id).updateView();
    }

    private void removeDbView(DbInfo dbInfo) {
        listViewDatabases.getItems().remove(DbViewItem.of(dbInfo.id).container);
    }

    void addDbView(DbInfo dbInfo) {
        listViewDatabases.getItems().add(DbViewItem.of(dbInfo.id).container);
    }

    public void addDbDialog() {
        AddDb.currentDbInfo = null;
        AppBuilder.showDialog(AddDb.class);
    }

    public void setViewDisable(boolean value) {
        Platform.runLater(() -> {
            buttonAddDb.setDisable(value);
            DbViewItem.itemMap.values().forEach(v -> v.setDisable(value));
            btnUpdateToLatest.setDisable(value);
            btnUpdateToRev.setDisable(value);
            textFieldRev.setDisable(value);
            btnSelectRev.setDisable(value);
        });
    }

    public void updateToRev() {
        setViewDisable(true);
        update(textFieldRev.getText());
    }

    public void updateToLatest() {
        setViewDisable(true);
        update(labelLastRev.getText());
    }

    int doneCount = 0;
    List<DbViewItem> selectedItems;

    private void update(String rev) {
        setStatus("starting...");
        FileUt.deleteFile(Config.get.updateErrorsFilePath);
        if (rev.trim().isEmpty()) {
            setStatus("please input revision!");
            setViewDisable(false);
            return;
        }
        DbViewItem.itemMap.values().forEach(i -> i.setStatus(""));
        selectedItems = DbViewItem.itemMap.values().stream().filter(r -> r.isSelected).collect(Collectors.toList());
        if (selectedItems.isEmpty()) {
            setStatus("no database selected!");
            setViewDisable(false);
            return;
        }
        doneCount = 0;
        new Thread(() -> {
            try {
                Svn.update(Config.get.svnDir);
                long toRev = Long.parseLong(rev);
                Collection<SVNLogEntry> revs = Svn.findRevisions();
                long lastRev = 0;
                Long selectedRev = null;
                for (SVNLogEntry svnLog : revs) {
                    if (svnLog.getRevision() >= toRev) {
                        selectedRev = svnLog.getRevision();
                        break;
                    }
                    lastRev = svnLog.getRevision();
                }
                if (selectedRev == null && lastRev <= toRev) {
                    selectedRev = lastRev;
                }
                if (selectedRev != null) {
                    long r = selectedRev;
                    selectedItems.forEach(i -> i.update(r, this::done));
                } else {
                    setStatus(String.format("Revision %s not valid", toRev));
                }
            } catch (Exception e) {
                e.printStackTrace();
                setStatus("Cannot find SVN Status of revision " + rev);
            }
        }).start();
    }


    @FXML Button buttonViewErrorsFile;
    public void done() {
        doneCount++;
        if (doneCount >= selectedItems.size()) {
            if (FileUt.exist(Config.get.updateErrorsFilePath)) {
                setStatus("view logs to check errors on some statements, other successfully!");
                buttonViewErrorsFile.setVisible(true);
            } else {
                setStatus("done successfully!");
                setViewDisable(false);
            }
        }
    }

    public void openErrors() throws IOException {
        Desktop.getDesktop().open(new File(Config.get.updateErrorsFilePath).getParentFile());
    }

    void setStatus(String status) {
        Platform.runLater(() -> labelStatus.setText(status));
    }

    public void selectRev() {
        SelectRev.onSelected = (rev) -> textFieldRev.setText(String.valueOf(rev));
        AppBuilder.showDialog(SelectRev.class);
    }
}
