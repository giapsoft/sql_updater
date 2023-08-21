package builder.data_table;

import app.AppState;
import mapper.Table;
import ui_model.AButton;
import ui_model.AppBuilder;
import ui_model.Txt;
import working.WorkingAction;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class AddRowForm extends AppBuilder {
    Map<String, String> pkColumns = new HashMap<>();
    Map<String, String> otherColumns = new HashMap<>();

    @Override
    public String getTitle() {
        return String.format("Add Row to Table %s", AppState.getTableName());
    }

    public AddRowForm() {
        AppState.currentTable().getPkColumns().forEach(n -> pkColumns.put(n, ""));
        AppState.currentTable().getOtherColumns().forEach(n -> otherColumns.put(n, ""));

        setLayout(new GridLayout(0, 1));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        pkColumns.keySet().forEach(pk -> {
            add(formInput(Txt.required(pk), text -> pkColumns.put(pk, text), text -> text.trim().length() >= 1 ? null : "At least 1 chars"));
        });
        otherColumns.keySet().forEach(other -> {
            add(formInput(new Txt(other), text -> otherColumns.put(other, text)));
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttons.add(new AButton("OK", this::onSave));
        buttons.add(new AButton("CANCEL", this::closeDialog));
        add(buttons);
        add(new Txt().boldRed("(*)").add(": required field").toLabel());

    }


    public void onSave() {
        if (isSubmitValid()) {
            AppState.addRow(pkColumns, otherColumns);
        } else {
            error("Please input all valid info");
        }
    }
}
