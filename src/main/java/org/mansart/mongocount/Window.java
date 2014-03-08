package org.mansart.mongocount;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ExtensionFileFilter;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public final class Window extends JPanel implements Counter.Listener {
    private Counter counter;
    private XYSeries data = null;

    private JFreeChart chart = null;
    private ChartPanel chartPanel = null;
    private JTextField dbnameField = new JTextField();
    private JTextField collnameField = new JTextField();
    private JSpinner intervalField = new JSpinner();
    private JButton startButton = new JButton("Refresh");
    private JButton stopButton = new JButton("Freeze");
    private JButton saveButton = new JButton("Save");
    private JButton quitButton = new JButton("Quit");

    public Window(Counter counter) {
        super();

        this.counter = counter;
        this.counter.addListener(this);

        this.setupChart();
        this.setupGraphics();
        this.setupListeners();
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

    private void setupGraphics() {
        this.intervalField.setValue(1);

        JPanel controlsPanel = new JPanel();
        controlsPanel.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(5, 5, 5, 5),
            new TitledBorder("Controls")));
        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.LINE_AXIS));
        controlsPanel.add(new JLabel("Database"));
        controlsPanel.add(this.dbnameField);
        controlsPanel.add(new JLabel("Collection"));
        controlsPanel.add(this.collnameField);
        controlsPanel.add(new JLabel("Interval"));
        controlsPanel.add(this.intervalField);
        controlsPanel.add(this.startButton);
        controlsPanel.add(this.stopButton);
        controlsPanel.add(new JSeparator(SwingConstants.VERTICAL));
        controlsPanel.add(this.saveButton);
        controlsPanel.add(new JSeparator(SwingConstants.VERTICAL));
        controlsPanel.add(this.quitButton);

        this.chartPanel = new ChartPanel(this.chart);
        this.chartPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        this.setLayout(new BorderLayout());
        this.add(this.chartPanel, BorderLayout.CENTER);
        this.add(controlsPanel, BorderLayout.SOUTH);
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

    public void setupListeners() {
        this.startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String dbname = Window.this.dbnameField.getText();
                String collname = Window.this.collnameField.getText();
                int interval = (Integer) Window.this.intervalField.getValue();
                Window.this.data.clear();
                if (!dbname.isEmpty() && !collname.isEmpty() && interval > 0) {
                    Window.this.chart.setTitle(dbname + "." + collname);
                    Window.this.counter.start(dbname, collname, interval);
                }
            }
        });

        this.stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Window.this.counter.stop();
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

    public static void main(String[] args) throws Exception {
        final Counter counter = new Counter();
        counter.connect();

        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Window window = new Window(counter);
        JFrame frame = new JFrame("Mongo Counter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setSize(900, 600);
        frame.setContentPane(window);
        frame.setVisible(true);
    }
}
