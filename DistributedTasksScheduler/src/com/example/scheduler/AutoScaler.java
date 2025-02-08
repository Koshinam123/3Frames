
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Queue;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AutoScaler {
    private static final int MIN_WORKERS = 2;
    private static final int MAX_WORKERS = 5;
    private static Queue<WorkerNode> workerPool = new ConcurrentLinkedQueue<>();

    static {
        initializeWorkers();
    }

    private static void initializeWorkers() {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT worker_name FROM worker_nodes WHERE status = 'ACTIVE'")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                workerPool.add(new WorkerNode(rs.getString("worker_name")));
            }
            if (workerPool.isEmpty()) {
                addWorkers(MIN_WORKERS);
            }
        } catch (SQLException e) {
            System.err.println("Error initializing workers: " + e.getMessage());
        }
    }

    public static WorkerNode getAvailableWorker() {
        WorkerNode worker = workerPool.poll();
        if (worker == null) {
            System.out.println("No available workers, adding one...");
            addWorkers(1);
            worker = workerPool.poll();
        }
        return worker;
    }

    public static void returnWorker(WorkerNode worker) {
        workerPool.add(worker);
    }

    public static void scaleWorkers(int taskCount) {
        int currentWorkers = workerPool.size();

        if (taskCount > currentWorkers && currentWorkers < MAX_WORKERS) {
            int newWorkers = Math.min(MAX_WORKERS - currentWorkers, taskCount - currentWorkers);
            addWorkers(newWorkers);
        } else if (taskCount < currentWorkers && currentWorkers > MIN_WORKERS) {
            int removeWorkers = Math.min(currentWorkers - MIN_WORKERS, currentWorkers - taskCount);
            removeWorkers(removeWorkers);
        }
    }

    private static void addWorkers(int count) {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO worker_nodes (worker_name, status) VALUES (?, 'ACTIVE')")) {
            for (int i = 0; i < count; i++) {
                String workerName = "Worker-" + (workerPool.size() + 1);
                stmt.setString(1, workerName);
                stmt.executeUpdate();
                workerPool.add(new WorkerNode(workerName));
            }
            System.out.println("Added " + count + " new workers.");
        } catch (SQLException e) {
            System.err.println("Error adding workers: " + e.getMessage());
        }
    }

    private static void removeWorkers(int count) {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE worker_nodes SET status = 'INACTIVE' WHERE worker_name = ?")) {
            for (int i = 0; i < count; i++) {
                WorkerNode worker = workerPool.poll();
                if (worker != null) {
                    stmt.setString(1, worker.getName());
                    stmt.executeUpdate();
                }
            }
            System.out.println("Removed " + count + " workers.");
        } catch (SQLException e) {
            System.err.println("Error removing workers: " + e.getMessage());
        }
    }

    public static void start() {
        System.out.println("Auto-Scaler Running...");
    }

    public static void addWorker(String workerName) {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO worker_nodes (worker_name, status) VALUES (?, 'ACTIVE')")) {
            stmt.setString(1, workerName);
            stmt.executeUpdate();
            workerPool.add(new WorkerNode(workerName));
            System.out.println("Worker " + workerName + " added successfully.");
        } catch (SQLException e) {
            System.err.println("Error adding worker: " + e.getMessage());
        }
    }

    public static List<WorkerNode> getAllWorkers() throws SQLException {
        List<WorkerNode> workers = new ArrayList<>();
        String sql = "SELECT worker_name FROM worker_nodes WHERE status = 'ACTIVE'";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                workers.add(new WorkerNode(rs.getString("worker_name")));
            }
        }
        return workers;
    }
}
