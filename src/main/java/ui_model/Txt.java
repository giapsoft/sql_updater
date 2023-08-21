package ui_model;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class Txt {
    public static int RED = 0;
    public static int BOLD = 1;
    public static int ITALIC = 2;

    public Txt(String text, int... options) {
        add(text, options);
    }

    public Txt() {
    }

    String formattedText = "";
    String rawText = "";



    public Txt add(String text, int... options) {
        rawText += text;
        List<String> styles = new ArrayList<>();
        for (int o : options) {
            if (o == RED) {
                styles.add("color: red");
            }
            if (o == BOLD) {
                styles.add("font-weight: bold");
            }
            if (o == ITALIC) {
                styles.add("font-style: italic");
            }
        }
        String item = String.format("<span style=\"%s\">%s</span>", String.join(";", styles), text);
        this.formattedText += item;
        return this;
    }

    public Txt boldRed(String text) {
        return add(text, BOLD, RED);
    }

    public Txt bold(String text) {
        return add(text, BOLD);
    }

    public Txt italic(String text) {
        return add(text, ITALIC);
    }

    public Txt br() {
        this.formattedText += "<br/>";
        return this;
    }

    public String get() {
        return String.format("<html><p>%s</p></html>", formattedText);
    }

    public String raw() {
        return rawText;
    }

    public JLabel toLabel() {
        return new JLabel(get());
    }

    public static Txt required(String label) {
        return new Txt(label).add(" *", RED, BOLD);
    }


}
