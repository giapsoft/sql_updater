package app.forms.capture;

import app.common.*;
import database_util.DatabaseRunner;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.tmatesoft.svn.core.SVNException;
import setup.Config;
import util.Svn;
import util.Timer;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class Capture extends AppBuilder {

    @FXML
    TextField ctrlUrl;
    @FXML
    TextField ctrlUser;
    @FXML
    TextField ctrlPassword;

    @FXML
    TextField mainUrl;
    @FXML
    TextField mainUser;
    @FXML
    TextField mainPassword;

    @FXML
    Button captureBtn;

    @FXML
    Label ctrlStatus;
    @FXML
    Label mainStatus;

    @FXML
    Label labelStatus;

    public void capture() throws IOException {
        AppState.get.reset();
        new Thread(this::doCapture).start();
    }

    DbCapture ctrlCapture;
    DbCapture mainCapture;

    void captureCtrl() {
        Platform.runLater(() -> ctrlStatus.setText("starting..."));
        DatabaseRunner ctrlDb = new DatabaseRunner(ctrlUrl.getText(), ctrlUser.getText(), ctrlPassword.getText());
        ctrlCapture = new DbCapture(AppState.generalCat, ctrlDb, DataType.ctrl);
        ctrlCapture.capture((txt) -> Platform.runLater(() -> ctrlStatus.setText(txt)));
        donePart();
    }

    void captureMain() {
        Platform.runLater(() -> mainStatus.setText("starting..."));
        DatabaseRunner mainDb = new DatabaseRunner(mainUrl.getText(), mainUser.getText(), mainPassword.getText());
        mainCapture = new DbCapture(AppState.generalCat, mainDb, DataType.main);
        mainCapture.capture((txt) -> Platform.runLater(() -> mainStatus.setText(txt)));
        donePart();
    }

    void doCapture() {
        Working.get.reset();
        AppHistory.get.reset();
        setDisabled(true);
        new Thread(this::captureCtrl).start();
        new Thread(this::captureMain).start();
    }

    int doneCount = 0;

    void donePart() {
        doneCount++;
        if (doneCount == 2) {
            ctrlCapture.mergeToWorking();
            mainCapture.mergeToWorking();
            Working.get.doMerge();
            try {
                AppConfig.get().setMinimumRevision(Svn.lastRev());
                Svn.commit(Config.get.svnDir, "Captured");
                Platform.runLater(() -> {
                    AppState.get.selectDataType(DataType.ctrl);
                    labelStatus.setText("done!");
                });
                setDisabled(false);
                AppBuilder.showInfo("Success", "Captured Successfully!");
            } catch (SVNException e) {
                AppHistory.get.reset();
                AppConfig.get().reset();
                AppBuilder.showError("error!", String.format("%s\r\n%s\r\n%s\r\n", Config.get.svnDir, "please revert SVN Dir and try again!", e.getMessage()));
            } finally {
                Working.get.reset();
            }
        }
    }

    void setDisabled(boolean status) {
        Platform.runLater(() -> {
            captureBtn.setDisable(status);
            ctrlUrl.setDisable(status);
            ctrlUser.setDisable(status);
            ctrlPassword.setDisable(status);
            mainUser.setDisable(status);
            mainUrl.setDisable(status);
            mainPassword.setDisable(status);
        });
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ctrlStatus.setText("");
        mainStatus.setText("");
        labelStatus.setText("");
        if (Working.get.getCaptureInfo() != null) {
            ctrlUrl.setText(Working.get.getCaptureInfo().getCtrlUrl());
            ctrlUser.setText(Working.get.getCaptureInfo().getCtrlUser());
            ctrlPassword.setText(Working.get.getCaptureInfo().getCtrlPass());
            mainUrl.setText(Working.get.getCaptureInfo().getMainUrl());
            mainUser.setText(Working.get.getCaptureInfo().getMainUser());
            mainPassword.setText(Working.get.getCaptureInfo().getMainPass());
        } else {
            Working.get.setCaptureInfo(new CaptureInfo());
        }
        for (TextField field : Arrays.asList(ctrlUrl, ctrlUrl, ctrlPassword, mainUrl, mainUser, mainPassword)) {
            field.textProperty().addListener(l -> updateCaptureInfo());
        }
    }

    Timer timer;

    void updateCaptureInfo() {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer(() -> {
            Working.get.getCaptureInfo().setCtrlUser(ctrlUser.getText());
            Working.get.getCaptureInfo().setCtrlPass(ctrlPassword.getText());
            Working.get.getCaptureInfo().setCtrlUrl(ctrlUrl.getText());

            Working.get.getCaptureInfo().setMainUser(mainUser.getText());
            Working.get.getCaptureInfo().setMainPass(mainPassword.getText());
            Working.get.getCaptureInfo().setMainUrl(mainUrl.getText());

            Working.get.save();
        });
    }
}
