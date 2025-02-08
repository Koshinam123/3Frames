public class Task {
    private int id;
    private String name;
    private String status;
    private String workerName;
    private int retryCount;

    public Task(int id, String name, String status, int retryCount, String workerName) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.retryCount = retryCount;
        this.workerName = workerName;
    }

    public int getId(){ 
        return id; 
    }
    public String getName(){
         return name;
 }
    public String getStatus(){
        return status; 
}
    public String getWorkerName(){ 
        return workerName; 
}
    public int getRetryCount() { 
        return retryCount; 
    }
}
