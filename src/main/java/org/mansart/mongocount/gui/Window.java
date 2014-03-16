package org.mansart.mongocount.gui;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.*;
import org.jfree.ui.ExtensionFileFilter;
import org.mansart.mongocount.Configuration;
import org.mansart.mongocount.Counter;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public final class Window extends JPanel {
    private Configuration configuration = null;
    private Counter counter = null;
    private TimeSeries data = null;

    private JFreeChart chart = null;
    private ChartPanel chartPanel = null;
    private JButton configurationButton = new JButton("Configure");
    private JButton lifeButton = new JButton("Start");
    private JButton clearButton = new JButton("Clear");
    private JButton saveButton = new JButton("Save");
    private JButton quitButton = new JButton("Quit");
    private JDialog configurationDialog = null;

    public Window(Configuration configuration, Counter counter) {
        super();

        this.configuration = configuration;
        this.counter = counter;

        this.configurationDialog = new ConfigurationDialog(configuration, this);

        this.setupListeners();
        this.setupChart();
        this.setupGraphics();
    }

    private void setupListeners() {
        this.configuration.addListener(new Configuration.Listener() {

            @Override
            public void onUpdate() {
                String dbname = Window.this.configuration.getDbname();
                String collname = Window.this.configuration.getCollname();
                final String title = dbname + "." + collname;
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        Window.this.chart.setTitle(title);
                        Color color = Window.this.configuration.getColor();
                        Window.this.chart.getXYPlot().getRenderer().setSeriesPaint(0, color);
                    }
                });
            }

            @Override
            public void onError() {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        JOptionPane.showMessageDialog(Window.this,
                            "Invalid configuration provided",
                            "Configuration invalid",
                            JOptionPane.ERROR_MESSAGE);
                    }
                });
            }
        });

        this.counter.addListener(new Counter.Listener() {

            @Override
            public void onStart() {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        Window.this.data.clear();
                        Window.this.lifeButton.setText("Stop");
                    }
                });
            }

            @Override
            public void onStop() {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        Window.this.lifeButton.setText("Start");
                    }
                });
            }

            @Override
            public void onCount(final long count) {
                String dbname = Window.this.configuration.getDbname();
                String collname = Window.this.configuration.getCollname();
                final String title = dbname + "." + collname + " : " + count;
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        Window.this.chart.setTitle(title);
                        Window.this.data.add(new Millisecond(), count);
                    }
                });
            }

            @Override
            public void onError() {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        JOptionPane.showMessageDialog(Window.this,
                            "An error occured while counting : " + Window.this.configuration,
                            "Processing error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                });
            }
        });

        this.configurationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Window.this.configurationDialog.setLocationRelativeTo(Window.this);
                Window.this.configurationDialog.setVisible(true);
            }
        });

        this.lifeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (Window.this.counter.isStarted()) {
                            Window.this.counter.stop();
                        } else {
                            Window.this.counter.start();
                        }
                    }
                }).start(); // Must not be processed in EDT thread
            }
        });

        this.clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Window.this.data.clear();
            }
        });

        this.saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.addChoosableFileFilter(new ExtensionFileFilter("JPEG", ".jpg"));
                int option = fileChooser.showSaveDialog(Window.this);
                if (option == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    int width = Window.this.chartPanel.getWidth();
                    int height = Window.this.chartPanel.getHeight();
                    try {
                        ChartUtilities.saveChartAsJPEG(file, Window.this.chart, width, height);
                    } catch (IOException exception) {
                        exception.printStackTrace();
                    }
                }
            }
        });

        this.quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
    }

    private void setupChart() {
        this.data = new TimeSeries("Counts");

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(this.data);

        DateAxis domain = new DateAxis("Time");
        domain.setAutoRange(true);
        NumberAxis range = new NumberAxis("Count");

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setBaseShapesVisible(false);
        renderer.setBaseSeriesVisibleInLegend(false);

        XYPlot plot = new XYPlot(dataset, domain, range, renderer);
        plot.setDomainCrosshairVisible(true);
        plot.setDomainCrosshairLockedOnData(false);
        plot.setRangeCrosshairVisible(false);

        this.chart = new JFreeChart("Mongo Counts", plot);
    }

    private void setupGraphics() {
        JPanel controlsPanel = new JPanel();
        controlsPanel.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(5, 5, 5, 5),
            new TitledBorder("Controls")));
        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.LINE_AXIS));
        controlsPanel.add(this.configurationButton);
        controlsPanel.add(Box.createHorizontalGlue());
        controlsPanel.add(this.lifeButton);
        controlsPanel.add(this.clearButton);
        controlsPanel.add(this.saveButton);
        controlsPanel.add(Box.createHorizontalGlue());
        controlsPanel.add(this.quitButton);

        this.chartPanel = new ChartPanel(this.chart);
        this.chartPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        this.setLayout(new BorderLayout());
        this.add(this.chartPanel, BorderLayout.CENTER);
        this.add(controlsPanel, BorderLayout.SOUTH);
    }
}
