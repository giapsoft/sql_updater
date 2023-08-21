package builder.data_table;

import app.AppState;
import ui_model.AButton;
import ui_model.AppBuilder;
import ui_model.Txt;
import util.StringUt;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class DataTable extends AppBuilder {
    JLabel nameLabel = new JLabel();
    JPanel pane = new JPanel();

    final AButton addColumnBtn = new AButton("+ Column", () -> {
        String colName = JOptionPane.showInputDialog("input new colum name");
        if (colName != null && !colName.trim().isEmpty()) {
            if (AppState.currentTable().hasCol(colName)) {
                error(String.format("column %s already existed", colName));
            } else {
                AppState.addColumn(colName);
            }
        }
    });

    final AButton addRowBtn = new AButton("+ Row", () -> new AddRowForm().showDialog());
    final AButton removeRowBtn = new AButton("- Row", () -> new DeleteRowForm().showDialog());

    public DataTable() {
        pane.setLayout(new GridBagLayout());
        pane.setBorder(new CompoundBorder(new TitledBorder("Table Info"), new EmptyBorder(0, 0, 0, 0)));
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weighty = 1;
        c.weightx = 1;
        nameLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        pane.add(nameLabel, c);

        c.gridy = 1;
        pane.add(addColumnBtn, c);
        c.gridx = 1;
        pane.add(addRowBtn, c);
        c.gridx = 2;
        pane.add(removeRowBtn, c);
        c.gridy = 2;
        c.gridx = 0;
        add(pane);

        AppState.listenTable((name, type) -> {
            if (StringUt.isEmpty(name)) {
pane.setVisible(false);
            } else {
                nameLabel.setHorizontalAlignment(SwingConstants.LEFT);
                nameLabel.setText(new Txt().add("Table: ").boldRed(name)
                        .br().add("Pk: ")
                        .bold(String.join(", ", AppState.currentTable().getPkColumns()))
                        .br().add("Others: ")
                        .bold(String.join(", ", AppState.currentTable().getOtherColumns()))
                        .get());
                pane.setVisible(true);
            }
        });
    }


}

