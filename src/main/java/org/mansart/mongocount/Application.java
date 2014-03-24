package org.mansart.mongocount;

import org.mansart.mongocount.gui.Window;

import javax.swing.*;
import java.awt.*;

public final class Application {


    public static void main(String[] args) throws Exception {
        final Configuration configuration = new Configuration();
        final Counter counter = new Counter(configuration);

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

        // Must be processed in EDT thread
        SwingUtilities.invokeLater(() -> {
            Window window = new Window(configuration, counter);
            JFrame frame = new JFrame("Mongo Counter");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setSize(900, 600);
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            frame.setLocation(
                screen.width / 2 - frame.getSize().width / 2,
                screen.height / 2 - frame.getSize().height / 2
            );
            frame.setContentPane(window);
            frame.setVisible(true);
        });
    }
}
