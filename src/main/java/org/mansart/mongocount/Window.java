package org.mansart.mongocount;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public final class Window extends JPanel implements Counter.Listener {
    private Counter counter;
    private XYSeries data = null;

    private JFreeChart chart = null;
    private JTextField dbnameField = new JTextField();
    private JTextField collnameField = new JTextField();
    private JSpinner intervalField = new JSpinner();
    private JButton configureButton = new JButton("Refresh");
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
        controlsPanel.add(this.configureButton);
        controlsPanel.add(new JSeparator(SwingConstants.VERTICAL));
        controlsPanel.add(this.quitButton);

        ChartPanel chartPanel = new ChartPanel(this.chart);
        chartPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        this.setLayout(new BorderLayout());
        this.add(chartPanel, BorderLayout.CENTER);
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
        XYSplineRenderer renderer = new XYSplineRenderer();
        renderer.setBaseShapesVisible(false);
        renderer.setBaseSeriesVisibleInLegend(false);
        chart.getXYPlot().setRenderer(renderer);
    }

    public void setupListeners() {
        this.configureButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String dbname = Window.this.dbnameField.getText();
                String collname = Window.this.collnameField.getText();
                int interval = (Integer) Window.this.intervalField.getValue();
                Window.this.data.clear();
                if (!dbname.isEmpty() && !collname.isEmpty() && interval > 0) {
                    Window.this.chart.setTitle(dbname + "." + collname);
                    Window.this.counter.configure(dbname, collname, interval);
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
