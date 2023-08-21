package app.forms.export.select_rev;

import app.common.AppBuilder;
import app.common.AppConfig;
import app.forms.export.DbHolder;
import app.forms.export.DbInfo;
import app.icons.Icons;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import util.Svn;
import util.Timer;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SelectRev extends AppBuilder {

    public static Consumer<Long> onSelected;

    static List<SVNLogEntry> revs;
    List<SVNLogEntry> filterRevs;

    public void filter() {
        String searchKey = textFieldSearch.getText().trim().toLowerCase();
        filterRevs = revs == null || revs.isEmpty() ? new ArrayList<>() : searchKey.isEmpty() ? revs :
                revs.stream().filter(r -> String.format("%s%s%s%s", new SimpleDateFormat("yyyy-MM-dd").format(
                        r.getDate()), r.getMessage(), r.getAuthor(), r.getRevision())
                        .toLowerCase().contains(searchKey))
                        .collect(Collectors.toList());
    }

    @FXML
    VBox container;

    @FXML
    ListView<HBox> listViewRevs;

    @FXML
    Label labelStatus;

    @FXML
    TextField textFieldSearch;

    Timer timer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        labelStatus.setText("loading...");
        listViewRevs.setOnMouseClicked(l -> {
            if (listViewRevs.getSelectionModel().getSelectedItem() != null) {
                onSelected.accept(filterRevs.get(listViewRevs.getSelectionModel().getSelectedIndex()).getRevision());
                close();
            }

        });
        textFieldSearch.textProperty().addListener(l -> {
            if (timer != null) {
                timer.cancel();
            }
            timer = new Timer(this::updateView);
        });
        new Thread(() -> {
            try {
                long last = Svn.lastRev();
                if (revs == null || revs.get(0).getRevision() < last) {
                    revs = Svn.findRevisions().stream().filter(r -> r.getRevision() > AppConfig.get().getMinimumRevision()).collect(Collectors.toList());
                    revs.sort((r1, r2) -> (int) (r2.getRevision() - r1.getRevision()));
                }
                updateView();
            } catch (SVNException e) {
                labelStatus.setText(e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    void setStatus(String text) {
        Platform.runLater(() -> labelStatus.setText(text));
    }

    Map<Long, HBox> viewMap = new HashMap<>();

    void updateView() {
        if (revs.isEmpty()) {
            setStatus("no valid revision!");
            Platform.runLater(() -> listViewRevs.getItems().clear());
            return;
        }
        setStatus("filtering...");
        filter();
        setStatus("done.");
        Platform.runLater(() -> listViewRevs.getItems().clear());
        filterRevs.forEach(this::addRev);
    }

    private void addRev(SVNLogEntry e) {
        HBox view = viewMap.computeIfAbsent(e.getRevision(), r -> {
            HBox box = hBox(
                    Icons.id.label(String.valueOf(e.getRevision())),
                    Icons.user.label(e.getAuthor()),
                    Icons.date.label(new SimpleDateFormat("yyyy-MM-dd").format(e.getDate())),
                    Icons.message.label(e.getMessage().substring(0, Math.min(50, e.getMessage().length())))
            );

            box.onMouseClickedProperty().addListener(l -> {
                onSelected.accept(e.getRevision());
                close();
            });
            return box;
        });
        Platform.runLater(() -> {
            listViewRevs.getItems().add(view);
        });
    }


}
