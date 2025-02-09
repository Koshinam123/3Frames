

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskSchedulerService {

    public void start() {
        new Thread(() -> {
            while (true) {
                try {


                     List<Task> pendingTasks = getAllPendingTasks(); //Fetch all pending tasks
                    AutoScaler.scaleWorkers(pendingTasks.size());  //Adjust worker nodes dynamically
                    Task task = fetchNextTask();
                    if (task != null && canExecuteTask(task.getId())) {
                        assignTask(task);
                    }
                    Thread.sleep(2000);
                } catch (Exception e) {
                    System.err.println("Error scheduling task: " + e.getMessage());
                }
            }
        }).start();
    }

    public void createTask(String name) 
    {
        String sql = "INSERT INTO tasks (name, status, retry_count) VALUES (?, 'PENDING', 0)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error creating task: " + e.getMessage());
        }
    }

    public List<Task> getAllTasks() throws SQLException 
    {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks ORDER BY created_at";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                tasks.add(new Task(rs.getInt("id"), rs.getString("name"), rs.getString("status"),
                        rs.getInt("retry_count"), rs.getString("worker_name")));
            }
        }
        return tasks;
    }

    public Task fetchNextTask() throws SQLException 
    {
        String sql = "SELECT * FROM tasks WHERE status = 'PENDING' ORDER BY created_at LIMIT 1";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return new Task(rs.getInt("id"), rs.getString("name"), rs.getString("status"),
                        rs.getInt("retry_count"), rs.getString("worker_name"));
            }
        }
        return null;
    }

    public boolean canExecuteTask(int taskId) throws SQLException
     {
        String sql = "SELECT COUNT(*) FROM task_dependencies " +
                     "WHERE task_id = ? AND dependency_id IN (SELECT id FROM tasks WHERE status != 'COMPLETED')";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, taskId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) == 0;
        }
    }

    public void assignTask(Task task) 
    {
        WorkerNode worker = AutoScaler.getAvailableWorker();
        if (worker != null) {
            worker.executeTask(task);
            updateTaskStatus(task.getId(), "RUNNING", worker.getName());
            new Thread(() -> {
                try {
                    Thread.sleep(2000); // Simulating execution delay
                    updateTaskStatus(task.getId(), "COMPLETED", worker.getName());
                    AutoScaler.returnWorker(worker);
                } catch (InterruptedException e) {
                    System.err.println("Error completing task: " + task.getId());
                }
            }).start();
        }
    }

    public static void updateTaskStatus(int taskId, String status, String workerName)
     {
        String sql = "UPDATE tasks SET status = ?, worker_name = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setString(2, workerName);
            stmt.setInt(3, taskId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating task status: " + e.getMessage());
        }
    }

    public static void incrementRetryCount(int taskId)
    {
        String sql = "UPDATE tasks SET retry_count = retry_count + 1 WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, taskId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error incrementing retry count: " + e.getMessage());
        }
    }
     public List<Task> getAllPendingTasks() throws SQLException {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE status = 'PENDING' ORDER BY created_at";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                tasks.add(new Task(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("status"),
                    rs.getInt("retry_count"),
                    rs.getString("worker_name")
                   
                ));
            }
        }
        return tasks;
    }
    
}
