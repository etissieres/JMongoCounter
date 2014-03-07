package org.mansart.mongocount;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public final class Window extends JPanel implements Counter.Listener {
    private Counter counter;
    private XYSeries data = null;
    private ChartPanel chartPanel = null;

    public Window(Counter counter) {
        super();

        this.counter = counter;
        this.counter.addListener(this);

        this.setupChart();
        this.setupGraphics();
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
        this.setLayout(new BorderLayout());
        this.add(this.chartPanel, BorderLayout.CENTER);
    }

    private void setupChart() {
        this.data = new XYSeries("Counts");

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(data);

        JFreeChart chart = ChartFactory.createXYLineChart(
            "Mongo Counts",
            "Ticks",
            "Counts",
            dataset
        );
        XYSplineRenderer renderer = new XYSplineRenderer();
        renderer.setBaseShapesVisible(false);
        chart.getXYPlot().setRenderer(renderer);
        this.chartPanel = new ChartPanel(chart);
    }

    public static void main(String[] args) throws Exception {
        final Counter counter = new Counter("qos", "queue");
        counter.connect();

        Window window = new Window(counter);
        JFrame frame = new JFrame("Mongo Counter");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setSize(900, 600);
        frame.setContentPane(window);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                counter.disconnect();
                System.exit(0);
            }
        });

        frame.setVisible(true);

        new Thread(counter).start();
    }
}
