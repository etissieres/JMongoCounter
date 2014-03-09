package org.mansart.mongocount.gui;

import org.mansart.mongocount.Configuration;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public final class ConfigurationDialog extends JDialog {
    private static final Color[] COLORS = new Color[] {
        Color.BLACK,
        Color.BLUE,
        Color.GREEN,
        Color.MAGENTA,
        Color.RED,
        Color.YELLOW
    };

    private Configuration configuration = null;
    private JTextField dbnameField = new JTextField();
    private JTextField collnameField = new JTextField();
    private JSpinner intervalField = new JSpinner();
    private JComboBox<Color> colorField = new JComboBox<Color>(COLORS);
    private JButton cancelButton = new JButton("Cancel");
    private JButton okButton = new JButton("OK");

    public ConfigurationDialog(Configuration configuration, JComponent owner) {
        super((Frame) SwingUtilities.windowForComponent(owner), "Configuration", true);

        this.configuration = configuration;

        this.setupListeners();
        this.setupGraphics();
    }

    private void setupListeners() {
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                ConfigurationDialog.this.dbnameField.setText(ConfigurationDialog.this.configuration.getDbname());
                ConfigurationDialog.this.collnameField.setText(ConfigurationDialog.this.configuration.getCollname());
                ConfigurationDialog.this.intervalField.setValue(ConfigurationDialog.this.configuration.getInterval());
            }
        });

        this.colorField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color color = (Color) ConfigurationDialog.this.colorField.getSelectedItem();
                ConfigurationDialog.this.colorField.setBackground(color);
            }
        });

        this.okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String dbname = ConfigurationDialog.this.dbnameField.getText();
                String collname = ConfigurationDialog.this.collnameField.getText();
                int interval = (Integer) ConfigurationDialog.this.intervalField.getValue();
                Color color = (Color) ConfigurationDialog.this.colorField.getSelectedItem();
                ConfigurationDialog.this.configuration.udpate(dbname, collname, interval, color);
                ConfigurationDialog.this.setVisible(false);
            }
        });

        this.cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ConfigurationDialog.this.setVisible(false);
            }
        });
    }

    private void setupGraphics() {
        this.setSize(300, 200);

        this.colorField.setRenderer(new ColorRenderer());
        this.colorField.setSelectedItem(Color.RED);

        JLabel dbnameLabel = new JLabel("Database");
        dbnameLabel.setHorizontalAlignment(JLabel.RIGHT);
        JLabel collnameLabel = new JLabel("Collection");
        collnameLabel.setHorizontalAlignment(JLabel.RIGHT);
        JLabel intervalLabel = new JLabel("Interval");
        intervalLabel.setHorizontalAlignment(JLabel.RIGHT);
        JLabel colorLabel = new JLabel("Color");
        colorLabel.setHorizontalAlignment(JLabel.RIGHT);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        double labelWeightx = 0.1;
        double controlWeightx = 0.9;
        Insets labelInsets = new Insets(0, 0, 0, 0);
        Insets controlInsets = new Insets(0, 5, 0, 5);

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = labelWeightx;
        constraints.insets = labelInsets;
        formPanel.add(dbnameLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.weightx = controlWeightx;
        constraints.insets = controlInsets;
        formPanel.add(this.dbnameField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.weightx = labelWeightx;
        constraints.insets = labelInsets;
        formPanel.add(collnameLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.weightx = controlWeightx;
        constraints.insets = controlInsets;
        formPanel.add(this.collnameField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.weightx = labelWeightx;
        constraints.insets = labelInsets;
        formPanel.add(intervalLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 2;
        constraints.weightx = controlWeightx;
        constraints.insets = controlInsets;
        formPanel.add(this.intervalField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.weightx = labelWeightx;
        constraints.insets = labelInsets;
        formPanel.add(colorLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 3;
        constraints.weightx = controlWeightx;
        constraints.insets = controlInsets;
        formPanel.add(this.colorField, constraints);

        JPanel controlsPanel = new JPanel(new FlowLayout());
        controlsPanel.add(this.cancelButton);
        controlsPanel.add(this.okButton);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(controlsPanel, BorderLayout.SOUTH);
        this.setContentPane(mainPanel);
    }

    private class ColorRenderer extends BasicComboBoxRenderer {
        @Override
        public Component getListCellRendererComponent(
            JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            Color color = (Color) value;
            this.setText("");
            this.setBackground(color);
            return this;
        }
    }
}
