package app.forms.add_category;

import app.common.AppBuilder;
import app.common.AppState;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import util.StringUt;

import java.net.URL;
import java.util.ResourceBundle;

public class AddCategory extends AppBuilder {

    @FXML
    TextField textFieldCatName;

    @FXML
    Label labelError;

    String catName = "";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        labelError.setText("");
        textFieldCatName.textProperty().addListener((observable, oldValue, newValue) -> {
            validate();
        });
    }

    boolean validate() {
        String text = textFieldCatName.getText().trim();
        if (text.isEmpty()) {
            labelError.setText("At least 1 char");
        } else if (StringUt.isAlphaDigitSpace(text)) {
            String name = StringUt.lower_underline(text);
            if(AppState.get.data.getCatNames().contains(name)) {
                labelError.setText("Category " + name + " already existed");
            } else {
                catName = name;
                labelError.setText("");
            }

        } else {
            labelError.setText("accept [alpha, numeric, space] only");
        }
        return labelError.getText().isEmpty();
    }

    public void submit() {
        if (validate()) {
            AppState.get.addCategory(StringUt.lower_underline(catName));
            close();
        }
    }
}
