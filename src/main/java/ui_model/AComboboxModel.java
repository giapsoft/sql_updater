package ui_model;

import javax.swing.*;
import java.util.Collection;

public class AComboboxModel extends DefaultComboBoxModel<String> {


    public AComboboxModel  setValue(Collection<String> value) {
        this.removeAllElements();
        value.forEach(this::addElement);
        return this;
    }


}
