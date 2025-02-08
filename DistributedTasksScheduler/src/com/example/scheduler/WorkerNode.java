
public class WorkerNode {
    private String name;

    public WorkerNode(String name)
     {
        this.name = name;
        registerWorker(); //Resgiestering the worker in mysql database
    }

    public String getName()
    {
        return name;
    }

    public void executeTask(Task task)
     {
        System.out.println(name + " is executing Task: " + task.getId() + " - " + task.getName());
        try {
            updateWorkerStatus("ACTIVE"); //Marking the worker as active worker
            TaskSchedulerService.updateTaskStatus(task.getId(), "RUNNING", name);
            
            Thread.sleep(2000);//controlling the ececution of tasks
            
            if (Math.random() < 0.2){ 
                throw new Exception("Simulated Task Failure");
            }

            TaskSchedulerService.updateTaskStatus(task.getId(), "COMPLETED", name);
            System.out.println(name + " completed Task: " + task.getId());

        } catch (Exception e) {
            System.err.println(name + " failed Task: " + task.getId());
            handleTaskFailure(task);
        } finally {
            updateWorkerStatus("ACTIVE"); // Making a worker as active
        }
    }

    private void handleTaskFailure(Task task) 
    {
        if (task.getRetryCount() < 3) {
            TaskSchedulerService.incrementRetryCount(task.getId());
            TaskSchedulerService.updateTaskStatus(task.getId(), "PENDING", null);
            System.out.println("Retrying Task: " + task.getId());
        } else {
            TaskSchedulerService.updateTaskStatus(task.getId(), "FAILED", name);
            System.err.println("Task " + task.getId() + " permanently failed after 3 retries.");
        }
    }

    private void registerWorker() 
    {
        String sql = "INSERT INTO worker_nodes (worker_name, status) VALUES (?, 'ACTIVE') " +
                     "ON DUPLICATE KEY UPDATE status='ACTIVE', last_heartbeat=CURRENT_TIMESTAMP";
        try (var conn = DBUtil.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Error registering worker: " + e.getMessage());
        }
    }

    private void updateWorkerStatus(String status) {
        String sql = "UPDATE worker_nodes SET status=?, last_heartbeat=CURRENT_TIMESTAMP WHERE worker_name=?";
        try (var conn = DBUtil.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setString(2, name);
            stmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Error updating worker status: " + e.getMessage());
        }
    }
}
