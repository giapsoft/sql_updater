package ui_model;

import app.AppState;
import ui_model.AComboboxModel;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class FilterComboBox extends JComboBox<String> {
    List<String> original = new ArrayList<>();
    public FilterComboBox() {
        super(new AComboboxModel());
        this.setEditable(true);
        final JTextField txt = (JTextField) this.getEditor().getEditorComponent();
        txt.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent ke) {
                SwingUtilities.invokeLater(() -> filter(txt.getText()));
            }
        });

    }

    public AComboboxModel getAModel() {
        return (AComboboxModel) super.getModel();
    }

    public void setValue(Collection<String> value) {
        getAModel().setValue(value);
        original = new ArrayList<>(value);
    }

    @Override
    public void addItem(String item) {
        super.addItem(item);
        original.add(item);
    }

    public void filter(String enteredText) {
        if (!this.isPopupVisible()) {
            this.showPopup();
        }



        List<String> filterArray= original.stream().filter(o -> o.toLowerCase().contains(enteredText.toLowerCase())).collect(Collectors.toList());
        if (filterArray.size() > 0) {
            AComboboxModel model = (AComboboxModel) this.getModel();
            model.setValue(filterArray);
            JTextField txt = (JTextField) this.getEditor().getEditorComponent();
            txt.setText(enteredText);
        }
    }

}
