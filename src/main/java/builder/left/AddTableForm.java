package builder.left;

import app.AppState;
import ui_model.AButton;
import ui_model.AppBuilder;
import mapper.Table;
import ui_model.Txt;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddTableForm extends AppBuilder {
    public AddTableForm() {
        setLayout(new GridLayout(0, 1));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        add(formInput(Txt.required("Table Name"), text -> tableName = text, text -> text.trim().length() >= 5 ? null : "At least 5 chars"));
        add(formInput(Txt.required("Pk Columns"), text -> pk = Arrays.asList(text.split(",")), text -> text.trim().length() >= 5 ? null : "At least 5 chars"));
        add(formInput(new Txt("Other Columns"), text -> columns = Arrays.asList(text.split(","))));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttons.add(new AButton("OK", this::onSave));
        buttons.add(new AButton("CANCEL", this::closeDialog));
        add(buttons);
        add(new Txt().boldRed("(*)").add(": required field")
                .br()
                .add("each column name is separated by a comma ").boldRed("[ , ]")
                .toLabel());
    }

    @Override
    public String getTitle() {
        return "Add Table";
    }

    String tableName;
    List<String> pk = new ArrayList<>();
    List<String> columns = new ArrayList<>();

    public void onSave() {
        if (isSubmitValid()) {
            if (!AppState.existTable(tableName)) {
                Table table = new Table(tableName, pk, columns);
                AppState.addTable(table);
                closeDialog();
            } else {
                error("Table Name Already Existed");
            }
        } else {
            error("Please input all valid info");
        }
    }

}
