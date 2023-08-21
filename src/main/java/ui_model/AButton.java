package ui_model;

import javax.swing.*;

public class AButton extends JButton {
    public AButton(String name, Runnable onClick) {
        setText(name);
        addActionListener(l -> onClick.run());
    }
}
