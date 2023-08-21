package ui_model;

import util.Ui;

import javax.swing.*;
import java.awt.*;

public class FullTextInput extends AppBuilder {
    public JTextArea textArea = new JTextArea();
    String title;
    public FullTextInput(String title) {
        this.title = title;
        setLayout(new BorderLayout());
        setPreferredSize(Ui.get.screenSize(0.8));
        add(textArea, BorderLayout.CENTER);
    }

    @Override
    public String getTitle() {
        return title;
    }
}
