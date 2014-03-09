package org.mansart.mongocount;

import org.mansart.mongocount.gui.Window;

import javax.swing.*;

public final class Application {


    public static void main(String[] args) throws Exception {
        final Configuration configuration = new Configuration();
        final Counter counter = new Counter(configuration);
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

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Window window = new Window(configuration, counter);
                JFrame frame = new JFrame("Mongo Counter");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLocationRelativeTo(null);
                frame.setSize(900, 600);
                frame.setContentPane(window);
                frame.setVisible(true);
            }
        });
    }
}
