package app.forms.export.add_db;

import app.common.AppBuilder;
import app.forms.export.DbHolder;
import app.forms.export.DbInfo;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.UUID;

public class AddDb extends AppBuilder {

    public static DbInfo currentDbInfo;

    @FXML
    TextField textFieldUrl;
    @FXML
    TextField textFieldUser;
    @FXML
    TextField textFieldPassword;
    @FXML
    TextField textFieldLastRev;

    @FXML
    CheckBox checkBoxCtrl;

    @FXML
    Button btnSubmit;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (currentDbInfo != null) {
            textFieldUrl.setText(currentDbInfo.getUrl());
            textFieldUser.setText(currentDbInfo.getUser());
            textFieldPassword.setText(currentDbInfo.getPassword());
            textFieldLastRev.setText(currentDbInfo.getLastRevision().toString());
            checkBoxCtrl.setSelected(currentDbInfo.isCtrl());
            btnSubmit.setText("UPDATE");
        } else {
            btnSubmit.setText("ADD");
        }
    }

    public void submit() {
        Long rev = 0l;
        try {
            rev = Long.parseLong(textFieldLastRev.getText());
        } catch (Exception ex) {
            // ignored
        }
        String url = textFieldUrl.getText();
        String user = textFieldUser.getText();
        String password = textFieldPassword.getText();
        boolean isCtrl = checkBoxCtrl.isSelected();

        if (currentDbInfo == null) {
            DbInfo dbInfo = new DbInfo(UUID.randomUUID().toString(), url, user, password, rev, isCtrl);
            DbHolder.get.addDb(dbInfo);
        } else {
            currentDbInfo.setId(currentDbInfo.getId());
            currentDbInfo.setUrl(url);
            currentDbInfo.setUser(user);
            currentDbInfo.setPassword(password);
            currentDbInfo.setLastRevision(rev);
            currentDbInfo.setCtrl(isCtrl);
            DbHolder.get.updateDb(currentDbInfo);
        }
        close();
    }

}
