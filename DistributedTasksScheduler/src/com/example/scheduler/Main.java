

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TaskSchedulerService scheduler = new TaskSchedulerService();
            scheduler.start();
            new Dashboard(scheduler).setVisible(true);
        });
    }
}
