
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class Dashboard extends JFrame {
    private JTable taskTable, workerTable;
    private DefaultTableModel taskTableModel, workerTableModel;
    private TaskSchedulerService schedulerService;

    public Dashboard(TaskSchedulerService scheduler) {
        setTitle("Task Scheduler Dashboard");
        setSize(900, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        schedulerService = scheduler;

        //this is the Task Table
        taskTableModel = new DefaultTableModel();
        taskTable = new JTable(taskTableModel);
        taskTableModel.addColumn("ID");
        taskTableModel.addColumn("Name");
        taskTableModel.addColumn("Status");
        taskTableModel.addColumn("Worker");
        taskTableModel.addColumn("Retries");

       //workertable
        workerTableModel = new DefaultTableModel();
        workerTable = new JTable(workerTableModel);
        workerTableModel.addColumn("Worker Name");
        workerTableModel.addColumn("Status");

        loadTasks();
        loadWorkers();

       //buttons
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> {
            loadTasks();
            loadWorkers();
        });

        JButton addTaskButton = new JButton("Add Task");
        addTaskButton.addActionListener(e -> createTask());

        JButton addWorkerButton = new JButton("Add Worker");
        addWorkerButton.addActionListener(e -> addWorker());

        //layout of the panel
        JPanel panel = new JPanel();
        panel.add(refreshButton);
        panel.add(addTaskButton);
        panel.add(addWorkerButton);

        //Adding components to the frame
        setLayout(new BorderLayout());
        add(new JScrollPane(taskTable), BorderLayout.CENTER);
        add(new JScrollPane(workerTable), BorderLayout.EAST);
        add(panel, BorderLayout.SOUTH);
    }

    private void loadTasks() {
        taskTableModel.setRowCount(0);
        try {
            List<Task> tasks = schedulerService.getAllTasks();
            for (Task task : tasks) {
                taskTableModel.addRow(new Object[]{
                        task.getId(), task.getName(), task.getStatus(),
                        task.getWorkerName() != null ? task.getWorkerName() : "Unassigned",
                        task.getRetryCount()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading tasks", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadWorkers() {
        workerTableModel.setRowCount(0);
        try {
            List<WorkerNode> workers = AutoScaler.getAllWorkers();
            for (WorkerNode worker : workers) {
                workerTableModel.addRow(new Object[]{
                        worker.getName(), "ACTIVE"
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading workers", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createTask() {
        String taskName = JOptionPane.showInputDialog(this, "Enter task name:");
        if (taskName != null && !taskName.trim().isEmpty()) {
            schedulerService.createTask(taskName);
            loadTasks();
        }
    }

    private void addWorker() {
        String workerName = JOptionPane.showInputDialog(this, "Enter new worker name:");
        if (workerName != null && !workerName.trim().isEmpty()) {
            AutoScaler.addWorker(workerName);
            loadWorkers();
        }
    }
}
