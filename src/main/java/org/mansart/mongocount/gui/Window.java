package org.mansart.mongocount.gui;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
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

public final class Window extends JPanel implements Configuration.Listener, Counter.Listener {
    private Configuration configuration = null;
    private Counter counter = null;
    private XYSeries data = null;

    private JFreeChart chart = null;
    private ChartPanel chartPanel = null;
    private JButton configurationButton = new JButton("Configure");
    private JButton startButton = new JButton("Start");
    private JButton stopButton = new JButton("Stop");
    private JButton clearButton = new JButton("Clear");
    private JButton saveButton = new JButton("Save");
    private JButton quitButton = new JButton("Quit");
    private JDialog configurationDialog = null;

    public Window(Configuration configuration, Counter counter) {
        super();

        this.configuration = configuration;
        this.configuration.addListener(this);
        this.counter = counter;
        this.counter.addListener(this);

        this.configurationDialog = new ConfigurationDialog(configuration, this);

        this.setupChart();
        this.setupGraphics();
        this.setupListeners();
    }

    private void setupChart() {
        this.data = new XYSeries("Counts");

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(data);

        this.chart = ChartFactory.createXYLineChart(
            "Mongo Counts",
            "Ticks",
            "Counts",
            dataset
        );
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setBaseShapesVisible(false);
        renderer.setBaseSeriesVisibleInLegend(false);
        this.chart.getXYPlot().setRenderer(renderer);
    }

    private void setupGraphics() {
        JPanel controlsPanel = new JPanel();
        controlsPanel.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(5, 5, 5, 5),
            new TitledBorder("Controls")));
        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.LINE_AXIS));
        controlsPanel.add(this.configurationButton);
        controlsPanel.add(Box.createHorizontalGlue());
        controlsPanel.add(this.startButton);
        controlsPanel.add(this.stopButton);
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

    private void setupListeners() {
        this.configurationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Window.this.configurationDialog.setLocationRelativeTo(Window.this);
                Window.this.configurationDialog.setVisible(true);
            }
        });

        this.startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Window.this.counter.start();
            }
        });

        this.stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Window.this.counter.stop();
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

    @Override
    public void onConfigurationUpdate() {
        final String title = this.configuration.getDbname() + "." + this.configuration.getCollname();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Window.this.data.clear();
                Window.this.chart.setTitle(title);
            }
        });
    }

    @Override
    public void onCount(final long count) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Window.this.data.add(Window.this.data.getItemCount(), count);
            }
        });
    }
}
