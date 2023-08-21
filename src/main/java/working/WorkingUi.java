package working;

import app.AppState;
import model.DataType;
import ui_model.AButton;
import ui_model.AppBuilder;
import ui_model.Txt;
import util.Ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class WorkingUi extends AppBuilder {
    public static String lvl1 = "+   ";
    public static String lvl2 = "++  ";
    public static String lvl3 = "+++ ";
    final JScrollPane scrollPane = new JScrollPane(null, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    GridBagConstraints c = new GridBagConstraints();
    JPanel panel = new JPanel();

    public WorkingUi() {
        setLayout(new BorderLayout());
        resetConstraint();
        setBackground(Color.black);
        reload();
        WorkingAction.get.listen(this::reload);
        add(scrollPane);
    }

    void resetConstraint() {
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.weighty = 1;
    }

    public void reload() {
        resetConstraint();
        panel = new JPanel();
        scrollPane.setViewportView(panel);
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));
        c.gridy = -1;
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        addingCategories();
        addingTables();
        addingColumns();
        updatingRows();
        deletingRows();
        panel.add(Box.createVerticalGlue());

    }

    void addNext(JComponent component) {
        c.gridy = c.gridy + 1;
        panel.add(component);
    }

    public void addingCategories() {
        if (!WorkingAction.get.addingCategories.isEmpty()) {
            java.util.List<JComponent> comps = new ArrayList<>();
            WorkingAction.get.addingCategories.forEach(c -> {
                comps.add(leftRow(new AButton("del", () -> {
                    if (c.equals("general")) {
                        error("Cannot delete general category");
                    } else {
                        AppState.removeCategory(c);
                    }
                }), new JLabel(c)));
            });
            addNext(verticalGroup(new Txt().bold("Adding Categories:").get(), comps));
        }
    }

    public void addingTables() {
        if (!WorkingAction.get.addingTables.isEmpty()) {
            java.util.List<JComponent> comps = new ArrayList<>();
            WorkingAction.get.addingTables.forEach((dataType, tableMap) -> {
                comps.add(new JLabel(lvl1 + dataType.name() + ":"));
                tableMap.forEach((tableName, stringSetMap) -> {
                    Set<String> pk = stringSetMap.get("pk");
                    Set<String> others = stringSetMap.get("others");
                    String details = lvl2 + tableName + ": ";
                    details += "pk=[" + String.join(", ", pk) + "]";
                    if (others != null && !others.isEmpty()) {
                        details += ", others=[" + String.join(", ", others) + "]";
                    }
                    comps.add(leftRow(new AButton("del", () -> {
                       revertAddTable(dataType, tableName);
                    }), new JLabel(details)));
                });
            });
            addNext(verticalGroup(new Txt().bold("Adding Tables").get(), comps));
        }
    }

    void revertAddTable(DataType dataType, String tableName) {
        Map<String, Map<String, Set<String>>> data = WorkingAction.get.addingTables.get(dataType);
        data.remove(tableName);
        if (data.isEmpty()) {
            WorkingAction.get.addingTables.remove(dataType);
        }
        WorkingAction.save();
    }

    public void addingColumns() {
        if (!WorkingAction.get.addingColumns.isEmpty()) {
            java.util.List<JComponent> comps = new ArrayList<>();
            WorkingAction.get.addingColumns.forEach((dataType, tableMap) -> {
                comps.add(new JLabel(lvl1 + dataType.name() + ":"));
                tableMap.forEach((name, columns) -> {
                    String details = lvl2 + name + ": ";
                    details += "[" + String.join(", ", columns) + "]";
                    comps.add(leftRow(new AButton("del", () -> {
                    }), new JLabel(details)));
                });
            });
            addNext(verticalGroup(new Txt().bold("Adding Columns").get(), comps));
        }
    }

    public void updatingRows() {
        if (!WorkingAction.get.updatingRows.isEmpty()) {

            java.util.List<JComponent> comps = new ArrayList<>();
            WorkingAction.get.updatingRows.forEach((catName, dataTypeMapMap) -> {
                comps.add(new JLabel(lvl1 + catName + ":"));
                dataTypeMapMap.forEach((dataType, tableMap) -> {
                    comps.add(new JLabel(lvl2 + dataType.name() + ":"));
                    tableMap.forEach((tableName, idMap) -> {
                        comps.add(new JLabel(lvl3 + tableName + ":"));
                        idMap.forEach((id, columns) -> {
                            Map<String, String> pk = columns.get("pk");
                            Map<String, String> others = columns.get("others");
                            String pkString = pk.entrySet().stream().map(e -> String.format("%s=%s", e.getKey(), e.getValue())).collect(Collectors.joining(", "));
                            String rowString = String.format("pk=[%s]", pkString);
                            if (others != null && !others.isEmpty()) {
                                String otherString = others.entrySet().stream().map(e -> String.format("%s=%s", e.getKey(), e.getValue())).collect(Collectors.joining(", "));
                                rowString += String.format(", others=[%s]", otherString);
                            }
                            comps.add(leftRow(new AButton("del", () -> {
                                Map<DataType, Map<String, Map<String, Map<String, Map<String, String>>>>> cat = WorkingAction.get.updatingRows.get(catName);
                                Map<String, Map<String, Map<String, Map<String, String>>>> data = cat.get(dataType);
                                Map<String, Map<String, Map<String, String>>> table = data.get(tableName);
                                table.remove(id);
                                if (table.isEmpty()) {
                                    data.remove(tableName);
                                }
                                if (data.isEmpty()) {
                                    cat.remove(dataType);
                                }
                                if (cat.isEmpty()) {
                                    WorkingAction.get.updatingRows.remove(catName);
                                }
                                WorkingAction.save();
                            }), new JLabel(rowString.substring(0, Math.min(100, rowString.length())))));
                        });
                    });
                });
            });
            addNext(verticalGroup(new Txt().bold("Updating Rows").get(), comps));
        }
    }

    public void deletingRows() {
        if (!WorkingAction.get.deletingRows.isEmpty()) {
            java.util.List<JComponent> comps = new ArrayList<>();
            WorkingAction.get.deletingRows.forEach((catName, dataTypeMapMap) -> {
                comps.add(new JLabel(lvl1 + catName + ":"));
                dataTypeMapMap.forEach((dataType, tableMap) -> {
                    comps.add(new JLabel(lvl2 + dataType.name() + ":"));
                    tableMap.forEach((tableName, idMap) -> {
                        comps.add(new JLabel(lvl3 + tableName + ":"));
                        idMap.forEach((id, pk) -> {
                            String pkString = pk.entrySet().stream().map(e -> String.format("%s=%s", e.getKey(), e.getValue())).collect(Collectors.joining(", "));
                            String rowString = String.format("pk=[%s]", pkString);
                            comps.add(leftRow(new AButton("del", () -> {
                                Map<DataType, Map<String, Map<String, Map<String, String>>>> cat = WorkingAction.get.deletingRows.get(catName);
                                Map<String, Map<String, Map<String, String>>> data = cat.get(dataType);
                                Map<String, Map<String, String>> table = data
                                        .get(tableName);
                                table.remove(id);
                                if (table.isEmpty()) {
                                    data.remove(tableName);
                                }
                                if (data.isEmpty()) {
                                    cat.remove(dataType);
                                }
                                if (cat.isEmpty()) {
                                    WorkingAction.get.deletingRows.remove(catName);
                                }
                                WorkingAction.save();
                            }), new JLabel(rowString)));
                        });
                    });
                });
            });
            addNext(verticalGroup(new Txt().bold("Deleting Rows").get(), comps));
        }
    }
}
