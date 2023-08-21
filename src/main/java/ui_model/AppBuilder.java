package ui_model;

import util.Ui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class AppBuilder extends JPanel {
    private JDialog dialog;

    public void closeDialog() {
        dialog.setVisible(false);
    }

    public void showDialog() {
        showDialog(() -> {
        });
    }

    public void showDialog(Runnable onClosed) {
        if (dialog == null) {
            dialog = new JDialog();
            dialog.setModal(true);
            dialog.setTitle(getTitle());
            dialog.setContentPane(this);
            dialog.pack();
            final int x = (Ui.get.screenSize().width - dialog.getWidth()) / 2;
            final int y = (Ui.get.screenSize().height - dialog.getHeight()) / 2;
            dialog.setLocation(x, y);
            dialog.addComponentListener(new ComponentListener() {
                @Override
                public void componentResized(ComponentEvent e) {

                }

                @Override
                public void componentMoved(ComponentEvent e) {

                }

                @Override
                public void componentShown(ComponentEvent e) {

                }

                @Override
                public void componentHidden(ComponentEvent e) {
                    onClosed.run();
                }
            });
        }
        dialog.setVisible(true);
    }

    public JPanel formInput(Txt label, Consumer<String> onChanged, Component... trails) {
        return formInput(label, onChanged, text -> null, trails);
    }

    void listen(JTextComponent txt, Consumer<String> onChanged) {
        txt.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                onChanged.accept(txt.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                onChanged.accept(txt.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                onChanged.accept(txt.getText());
            }

        });
    }

    public JPanel formInput(Txt tLabel, Consumer<String> onChanged, Function<String, String> validator, Component... trails) {
        JPanel container = new JPanel(new GridLayout(0, 1));
        final JLabel errorLbl = new JLabel("", JLabel.RIGHT);
        errorLbl.setForeground(Color.red);
        JTextField txt = new JTextField(50);
        final boolean isRequired = tLabel.get().contains("*");
        Consumer<String> changed = text -> {
            String validateMsg = validator.apply(text);
            if (isRequired) {
                addError(tLabel.rawText, validateMsg);
            }

            if (validateMsg == null) {
                onChanged.accept(text);
                errorLbl.setText("");

            } else {
                errorLbl.setText(validateMsg);
            }
        };
        listen(txt, changed);

        if (isRequired) {
            requires.add(tLabel.raw());
        }
        errorLbl.setBorder(new EmptyBorder(-20, 0, 0, 0));
        JPanel inputRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        inputRow.add(new JLabel(tLabel.get(), JLabel.RIGHT));
        inputRow.add(txt);
        FullTextInput fullTextInput = new FullTextInput(tLabel.rawText);
        listen(fullTextInput.textArea, changed);
        inputRow.add(new AButton(">", () -> {
            fullTextInput.textArea.setText(txt.getText());
            fullTextInput.showDialog(() -> txt.setText(fullTextInput.textArea.getText()));
        }));
        container.add(inputRow);
        container.add(errorLbl);
        container.setBorder(new EmptyBorder(0, 0, -15, 0));
        if (trails != null) {
            for (Component c : trails) {
                inputRow.add(c);
            }
        }
        return container;
    }

    protected JComponent input(int length, Consumer<String> onInput, JComponent... trails) {
        JPanel inputRow = new JPanel();
        JTextField txt = new JTextField(length);
        inputRow.add(txt);
        listen(txt, onInput);
        if (trails != null) {
            for (JComponent component : trails) {
                inputRow.add(component);
            }
        }
        return inputRow;
    }


    protected JPanel leftRow(Component... components) {
        JPanel myRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        for (Component c : components) {
            myRow.add(c);
        }
        return myRow;
    }

    public String getTitle() {
        return "No Title";
    }

    protected void error(String text) {
        JOptionPane.showMessageDialog(this, text, "Error", JOptionPane.ERROR_MESSAGE);
    }

    protected Set<String> submitValid = new HashSet<>();
    protected java.util.Set<String> requires = new HashSet<>();

    protected void addError(String name, String text) {
        if (text == null) {
            submitValid.add(name);
        } else {
            submitValid.remove(name);
        }
    }

    protected boolean isSubmitValid() {
        return submitValid.containsAll(requires);
    }

    protected JPanel group(String title, List<Component> components) {
        return group(title, components.toArray(new Component[0]));
    }

    protected JPanel group(String title, Component... components) {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(new CompoundBorder(new TitledBorder(title), new EmptyBorder(0, 0, 0, 0)));
        for (Component component : components) {
            panel.add(component);
        }
        return panel;
    }

    protected JPanel verticalGroup(String title, List<JComponent> components) {
        return verticalGroup(title, components.toArray(new JComponent[0]));
    }

    protected JPanel verticalGroup(String title, JComponent... components) {
        JPanel panel = new JPanel();
        panel.setBorder(new CompoundBorder(new TitledBorder(title), new EmptyBorder(0, 0, 0, 0)));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        GridBagConstraints con = new GridBagConstraints();
        con.anchor = GridBagConstraints.NORTHWEST;
        con.fill = GridBagConstraints.HORIZONTAL;
        con.weightx = 1;
        con.weighty = 1;
        int y = 0;
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (JComponent component : components) {
            con.gridy = y++;
            component.setMaximumSize(component.getPreferredSize());
            component.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(component);
        }
        panel.add(Box.createVerticalGlue());
        return panel;
    }
}
