package builder;

import app.AppState;
import app.AppSummary;
import builder.left.AddTableForm;
import model.DataType;
import model.ListenType;
import ui_model.AButton;
import ui_model.AListModel;
import ui_model.AppBuilder;
import ui_model.FilterComboBox;
import working.WorkingAction;

import javax.swing.*;
import java.awt.*;
import java.util.stream.Collectors;

public class LeftPanel extends AppBuilder {

    String filterTableText = "";
    Runnable filterTables;

    public LeftPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.weighty = 1;
        add(categoriesUi(), c);
        c.gridy = 1;
        add(dataTypesUi(), c);
        c.gridy = 2;
        c.fill = GridBagConstraints.BOTH;
        add(verticalGroup("Tables", input(20, v -> {
                    filterTableText = v;
                    filterTables.run();
                }, new AButton("+", () -> new AddTableForm().showDialog())),
                tblFilter()), c);
        WorkingAction.get.listen(filterTables);
    }

    JComponent tblFilter() {
        AListModel<String> tblModel = new AListModel<>();
        JList<String> tblList = new JList<>(tblModel);
        filterTables = () -> {
                tblList.clearSelection();
                tblModel.setValue(AppState.currentTableNames().stream().filter(t -> t.toLowerCase().contains(filterTableText.toLowerCase())).collect(Collectors.toList()));
        };
        AppState.listenTable((name, type) -> {
            if (type.isChanged()) {
                tblList.setSelectedValue(name, true);
            } else if (!type.isSilence()) {
                filterTables.run();
            }
        });

        AppState.listenDataType(filterTables);

        tblList.setSelectedValue(AppState.getTableName(), true);
        tblList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblList.addListSelectionListener(l -> {
            if (!l.getValueIsAdjusting()) {
                AppState.setTableName(tblList.getSelectedValue(), ListenType.silence);
            }

        });

        filterTables.run();
        return tblList;
    }

    JPanel dataTypesUi() {
        return group("Data Type", dataTypesUi("Common", true), dataTypesUi("Ctrl", false), dataTypesUi("Main", false));
    }


    JPanel categoriesUi() {
        final FilterComboBox categories = new FilterComboBox();
        categories.setValue(AppSummary.allCategories());
        categories.setSelectedItem(AppState.getCategoryName());
        categories.setBounds(100, 100, 200, 20);
        categories.addActionListener(l -> {
            final Object selectedItem = categories.getSelectedItem();
            if (selectedItem != null) {
                AppState.setCategoryName(selectedItem.toString(), ListenType.silence);
            }
        });

        AppState.listenCategory((name, type) -> {
            if (type.isAdded()) {
                categories.addItem(name);
            } else if (type.isChanged()) {
                categories.setSelectedItem(name);
            }
        });


        JButton addBtn = new JButton("+");
        addBtn.addActionListener((event) -> {
            String catName = JOptionPane.showInputDialog("Add New Category");
            if (catName != null) {
                if (!catName.trim().isEmpty() && !AppSummary.allCategories().contains(catName)) {
                    AppState.addCategory(catName);
                } else {
                    error("Category already existed!");
                }
            }

        });
        return group("Category", categories, addBtn);
    }

    private final ButtonGroup dataTypeGroup = new ButtonGroup();

    JRadioButton dataTypesUi(String label, boolean selected) {
        JRadioButton radio = new JRadioButton(label);
        radio.setSelected(selected);
        dataTypeGroup.add(radio);
        radio.addActionListener(listener -> {
            AppState.setDataType(DataType.valueOf(listener.getActionCommand().toLowerCase()));
        });
        return radio;
    }
}
