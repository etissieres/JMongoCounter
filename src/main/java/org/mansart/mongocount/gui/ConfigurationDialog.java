package org.mansart.mongocount.gui;

import org.mansart.mongocount.Configuration;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public final class ConfigurationDialog extends JDialog {
    private Configuration configuration = null;
    private JTextField dbnameField = new JTextField();
    private JTextField collnameField = new JTextField();
    private JSpinner intervalField = new JSpinner();
    private JButton cancelButton = new JButton("Cancel");
    private JButton okButton = new JButton("OK");

    public ConfigurationDialog(Configuration configuration, JComponent owner) {
        super((Frame) SwingUtilities.windowForComponent(owner), "Configuration", true);

        this.configuration = configuration;

        this.setupGraphics();
        this.setupListeners();
    }

    private void setupGraphics() {
        this.setSize(300, 150);

        JLabel dbnameLabel = new JLabel("Database");
        dbnameLabel.setHorizontalAlignment(JLabel.RIGHT);
        JLabel collnameLabel = new JLabel("Collection");
        collnameLabel.setHorizontalAlignment(JLabel.RIGHT);
        JLabel intervalLabel = new JLabel("Interval");
        intervalLabel.setHorizontalAlignment(JLabel.RIGHT);

        JPanel formPanel = new JPanel(new GridLayout(3, 2));
        formPanel.add(dbnameLabel);
        formPanel.add(this.dbnameField);
        formPanel.add(collnameLabel);
        formPanel.add(this.collnameField);
        formPanel.add(intervalLabel);
        formPanel.add(this.intervalField);

        JPanel controlsPanel = new JPanel(new FlowLayout());
        controlsPanel.add(this.cancelButton);
        controlsPanel.add(this.okButton);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(controlsPanel, BorderLayout.SOUTH);
        this.setContentPane(mainPanel);
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

        this.okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String dbname = ConfigurationDialog.this.dbnameField.getText();
                String collname = ConfigurationDialog.this.collnameField.getText();
                int interval = (Integer) ConfigurationDialog.this.intervalField.getValue();
                ConfigurationDialog.this.configuration.udpate(dbname, collname, interval);
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
}
