package link.zzone.monitor;

/**
 * @author chrischen
 * @date 2025/8/7
 * @desc Event listener interface for task scheduling events
 */
public interface TaskSchedulingEventListener {
    
    void onTaskSubmitted(String taskId, String taskType);
    
    void onTaskStarted(String taskId, String taskType);
    
    void onTaskCompleted(String taskId, String taskType);
    
    void onTaskFailed(String taskId, String taskType, Throwable error);
    
    void onTaskSkipped(String taskId, String taskType);
    
    void onTaskStopped(String taskId, String taskType);
}