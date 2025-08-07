package link.zzone.monitor;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author chrischen
 * @date 2025/8/7
 * @desc Task metrics for specific task types
 */
public class TaskMetrics {
    
    private final String taskType;
    private final AtomicLong submitted = new AtomicLong(0);
    private final AtomicLong started = new AtomicLong(0);
    private final AtomicLong completed = new AtomicLong(0);
    private final AtomicLong failed = new AtomicLong(0);
    private final AtomicLong skipped = new AtomicLong(0);
    
    private final LongAdder totalExecutionTime = new LongAdder();
    private final AtomicLong maxExecutionTime = new AtomicLong(0);
    private final AtomicLong minExecutionTime = new AtomicLong(Long.MAX_VALUE);
    
    private final List<String> recentErrors = Collections.synchronizedList(new ArrayList<>());
    private final LocalDateTime createdTime = LocalDateTime.now();
    
    private static final int MAX_RECENT_ERRORS = 10;
    
    public TaskMetrics(String taskType) {
        this.taskType = taskType;
    }
    
    public void incrementSubmitted() {
        submitted.incrementAndGet();
    }
    
    public void incrementStarted() {
        started.incrementAndGet();
    }
    
    public void incrementCompleted() {
        completed.incrementAndGet();
    }
    
    public void incrementFailed() {
        failed.incrementAndGet();
    }
    
    public void incrementSkipped() {
        skipped.incrementAndGet();
    }
    
    public void recordError(Throwable error) {
        synchronized (recentErrors) {
            if (recentErrors.size() >= MAX_RECENT_ERRORS) {
                recentErrors.remove(0);
            }
            recentErrors.add(LocalDateTime.now() + ": " + error.getMessage());
        }
    }
    
    public void recordExecutionTime(long executionTimeMs) {
        totalExecutionTime.add(executionTimeMs);
        maxExecutionTime.updateAndGet(current -> Math.max(current, executionTimeMs));
        minExecutionTime.updateAndGet(current -> 
            current == Long.MAX_VALUE ? executionTimeMs : Math.min(current, executionTimeMs));
    }
    
    public String getTaskType() {
        return taskType;
    }
    
    public long getSubmitted() {
        return submitted.get();
    }
    
    public long getStarted() {
        return started.get();
    }
    
    public long getCompleted() {
        return completed.get();
    }
    
    public long getFailed() {
        return failed.get();
    }
    
    public long getSkipped() {
        return skipped.get();
    }
    
    public double getSuccessRate() {
        long total = submitted.get();
        return total > 0 ? (double) completed.get() / total * 100 : 0;
    }
    
    public double getFailureRate() {
        long total = submitted.get();
        return total > 0 ? (double) failed.get() / total * 100 : 0;
    }
    
    public long getAverageExecutionTime() {
        long completedTasks = completed.get();
        return completedTasks > 0 ? totalExecutionTime.sum() / completedTasks : 0;
    }
    
    public long getMaxExecutionTime() {
        return maxExecutionTime.get();
    }
    
    public long getMinExecutionTime() {
        long min = minExecutionTime.get();
        return min == Long.MAX_VALUE ? 0 : min;
    }
    
    public List<String> getRecentErrors() {
        synchronized (recentErrors) {
            return new ArrayList<>(recentErrors);
        }
    }
    
    public LocalDateTime getCreatedTime() {
        return createdTime;
    }
    
    @Override
    public String toString() {
        return String.format(
            "TaskMetrics{type='%s', submitted=%d, completed=%d, failed=%d, successRate=%.2f%%, avgExecTime=%dms}",
            taskType, submitted.get(), completed.get(), failed.get(), getSuccessRate(), getAverageExecutionTime()
        );
    }
}