package link.zzone.monitor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author chrischen
 * @date 2025/8/7
 * @desc Task Scheduling Monitor - Monitors task execution metrics and health
 */
public class TaskSchedulingMonitor {

    private final AtomicLong totalTasksSubmitted = new AtomicLong(0);
    private final AtomicLong totalTasksCompleted = new AtomicLong(0);
    private final AtomicLong totalTasksFailed = new AtomicLong(0);
    private final AtomicLong totalTasksSkipped = new AtomicLong(0);
    private final AtomicLong currentRunningTasks = new AtomicLong(0);
    private final AtomicLong currentWaitingTasks = new AtomicLong(0);
    
    private final LongAdder totalExecutionTime = new LongAdder();
    private final AtomicLong maxExecutionTime = new AtomicLong(0);
    private final AtomicLong minExecutionTime = new AtomicLong(Long.MAX_VALUE);
    
    private final ConcurrentHashMap<String, TaskMetrics> taskMetricsMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LocalDateTime> taskStartTimes = new ConcurrentHashMap<>();
    
    private volatile LocalDateTime startTime = LocalDateTime.now();
    private volatile boolean monitoringEnabled = true;

    public void recordTaskSubmitted(String taskId, String taskType) {
        if (!monitoringEnabled) return;
        
        totalTasksSubmitted.incrementAndGet();
        currentWaitingTasks.incrementAndGet();
        
        taskMetricsMap.computeIfAbsent(taskType, k -> new TaskMetrics(taskType))
                      .incrementSubmitted();
    }

    public void recordTaskStarted(String taskId, String taskType) {
        if (!monitoringEnabled) return;
        
        currentWaitingTasks.decrementAndGet();
        currentRunningTasks.incrementAndGet();
        taskStartTimes.put(taskId, LocalDateTime.now());
        
        taskMetricsMap.computeIfAbsent(taskType, k -> new TaskMetrics(taskType))
                      .incrementStarted();
    }

    public void recordTaskCompleted(String taskId, String taskType) {
        if (!monitoringEnabled) return;
        
        totalTasksCompleted.incrementAndGet();
        currentRunningTasks.decrementAndGet();
        
        recordExecutionTime(taskId);
        
        taskMetricsMap.computeIfAbsent(taskType, k -> new TaskMetrics(taskType))
                      .incrementCompleted();
    }

    public void recordTaskFailed(String taskId, String taskType, Throwable error) {
        if (!monitoringEnabled) return;
        
        totalTasksFailed.incrementAndGet();
        currentRunningTasks.decrementAndGet();
        
        recordExecutionTime(taskId);
        
        TaskMetrics metrics = taskMetricsMap.computeIfAbsent(taskType, k -> new TaskMetrics(taskType));
        metrics.incrementFailed();
        metrics.recordError(error);
    }

    public void recordTaskSkipped(String taskId, String taskType) {
        if (!monitoringEnabled) return;
        
        totalTasksSkipped.incrementAndGet();
        currentWaitingTasks.decrementAndGet();
        
        taskMetricsMap.computeIfAbsent(taskType, k -> new TaskMetrics(taskType))
                      .incrementSkipped();
    }

    private void recordExecutionTime(String taskId) {
        LocalDateTime startTime = taskStartTimes.remove(taskId);
        if (startTime != null) {
            long executionTime = java.time.Duration.between(startTime, LocalDateTime.now()).toMillis();
            totalExecutionTime.add(executionTime);
            
            maxExecutionTime.updateAndGet(current -> Math.max(current, executionTime));
            minExecutionTime.updateAndGet(current -> 
                current == Long.MAX_VALUE ? executionTime : Math.min(current, executionTime));
        }
    }

    public MonitoringReport generateReport() {
        long submitted = totalTasksSubmitted.get();
        long completed = totalTasksCompleted.get();
        long failed = totalTasksFailed.get();
        long skipped = totalTasksSkipped.get();
        long running = currentRunningTasks.get();
        long waiting = currentWaitingTasks.get();
        
        double successRate = submitted > 0 ? (double) completed / submitted * 100 : 0;
        double failureRate = submitted > 0 ? (double) failed / submitted * 100 : 0;
        
        long avgExecutionTime = completed > 0 ? totalExecutionTime.sum() / completed : 0;
        long maxExecTime = maxExecutionTime.get();
        long minExecTime = minExecutionTime.get() == Long.MAX_VALUE ? 0 : minExecutionTime.get();
        
        List<TaskMetrics> taskTypeMetrics = new ArrayList<>(taskMetricsMap.values());
        
        return new MonitoringReport(
            submitted, completed, failed, skipped, running, waiting,
            successRate, failureRate, avgExecutionTime, maxExecTime, minExecTime,
            taskTypeMetrics, startTime, LocalDateTime.now()
        );
    }

    public void reset() {
        totalTasksSubmitted.set(0);
        totalTasksCompleted.set(0);
        totalTasksFailed.set(0);
        totalTasksSkipped.set(0);
        currentRunningTasks.set(0);
        currentWaitingTasks.set(0);
        
        totalExecutionTime.reset();
        maxExecutionTime.set(0);
        minExecutionTime.set(Long.MAX_VALUE);
        
        taskMetricsMap.clear();
        taskStartTimes.clear();
        startTime = LocalDateTime.now();
    }

    public void enableMonitoring() {
        this.monitoringEnabled = true;
    }

    public void disableMonitoring() {
        this.monitoringEnabled = false;
    }

    public boolean isMonitoringEnabled() {
        return monitoringEnabled;
    }

    public long getTotalTasksSubmitted() {
        return totalTasksSubmitted.get();
    }

    public long getTotalTasksCompleted() {
        return totalTasksCompleted.get();
    }

    public long getTotalTasksFailed() {
        return totalTasksFailed.get();
    }

    public long getCurrentRunningTasks() {
        return currentRunningTasks.get();
    }

    public long getCurrentWaitingTasks() {
        return currentWaitingTasks.get();
    }

    public double getSuccessRate() {
        long submitted = totalTasksSubmitted.get();
        return submitted > 0 ? (double) totalTasksCompleted.get() / submitted * 100 : 0;
    }

    public List<String> getHealthWarnings() {
        List<String> warnings = new ArrayList<>();
        
        if (currentRunningTasks.get() > 1000) {
            warnings.add("High number of running tasks: " + currentRunningTasks.get());
        }
        
        if (currentWaitingTasks.get() > 5000) {
            warnings.add("High number of waiting tasks: " + currentWaitingTasks.get());
        }
        
        double failureRate = getTotalTasksSubmitted() > 0 ? 
            (double) totalTasksFailed.get() / getTotalTasksSubmitted() * 100 : 0;
        if (failureRate > 10) {
            warnings.add(String.format("High failure rate: %.2f%%", failureRate));
        }
        
        return warnings;
    }
}