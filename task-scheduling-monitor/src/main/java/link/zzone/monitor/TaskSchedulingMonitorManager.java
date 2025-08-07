package link.zzone.monitor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author chrischen
 * @date 2025/8/7
 * @desc Monitor manager that coordinates monitoring activities
 */
public class TaskSchedulingMonitorManager implements TaskSchedulingEventListener {
    
    private final TaskSchedulingMonitor monitor;
    private final List<TaskSchedulingEventListener> listeners = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "task-monitor-scheduler");
        t.setDaemon(true);
        return t;
    });
    
    private volatile boolean autoReporting = false;
    private volatile int reportIntervalSeconds = 60;
    
    public TaskSchedulingMonitorManager() {
        this.monitor = new TaskSchedulingMonitor();
        this.listeners.add(monitor);
    }
    
    public TaskSchedulingMonitorManager(TaskSchedulingMonitor monitor) {
        this.monitor = monitor;
        this.listeners.add(monitor);
    }
    
    public void addListener(TaskSchedulingEventListener listener) {
        listeners.add(listener);
    }
    
    public void removeListener(TaskSchedulingEventListener listener) {
        listeners.remove(listener);
    }
    
    public void startAutoReporting() {
        startAutoReporting(reportIntervalSeconds);
    }
    
    public void startAutoReporting(int intervalSeconds) {
        this.reportIntervalSeconds = intervalSeconds;
        this.autoReporting = true;
        
        scheduler.scheduleAtFixedRate(() -> {
            try {
                MonitoringReport report = monitor.generateReport();
                System.out.println("=== Scheduled Monitor Report ===");
                System.out.println(report.generateSummaryReport());
                
                List<String> warnings = monitor.getHealthWarnings();
                if (!warnings.isEmpty()) {
                    System.out.println("Health Warnings:");
                    warnings.forEach(warning -> System.out.println("  - " + warning));
                }
                System.out.println();
            } catch (Exception e) {
                System.err.println("Error generating monitoring report: " + e.getMessage());
            }
        }, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
    }
    
    public void stopAutoReporting() {
        this.autoReporting = false;
        scheduler.shutdown();
    }
    
    public TaskSchedulingMonitor getMonitor() {
        return monitor;
    }
    
    public MonitoringReport generateReport() {
        return monitor.generateReport();
    }
    
    public void printDetailedReport() {
        MonitoringReport report = monitor.generateReport();
        System.out.println(report.generateDetailedReport());
    }
    
    public void printSummaryReport() {
        MonitoringReport report = monitor.generateReport();
        System.out.println(report.generateSummaryReport());
    }
    
    // TaskSchedulingEventListener implementation
    @Override
    public void onTaskSubmitted(String taskId, String taskType) {
        listeners.forEach(listener -> {
            try {
                listener.onTaskSubmitted(taskId, taskType);
            } catch (Exception e) {
                System.err.println("Error in task submitted listener: " + e.getMessage());
            }
        });
    }
    
    @Override
    public void onTaskStarted(String taskId, String taskType) {
        listeners.forEach(listener -> {
            try {
                listener.onTaskStarted(taskId, taskType);
            } catch (Exception e) {
                System.err.println("Error in task started listener: " + e.getMessage());
            }
        });
    }
    
    @Override
    public void onTaskCompleted(String taskId, String taskType) {
        listeners.forEach(listener -> {
            try {
                listener.onTaskCompleted(taskId, taskType);
            } catch (Exception e) {
                System.err.println("Error in task completed listener: " + e.getMessage());
            }
        });
    }
    
    @Override
    public void onTaskFailed(String taskId, String taskType, Throwable error) {
        listeners.forEach(listener -> {
            try {
                listener.onTaskFailed(taskId, taskType, error);
            } catch (Exception e) {
                System.err.println("Error in task failed listener: " + e.getMessage());
            }
        });
    }
    
    @Override
    public void onTaskSkipped(String taskId, String taskType) {
        listeners.forEach(listener -> {
            try {
                listener.onTaskSkipped(taskId, taskType);
            } catch (Exception e) {
                System.err.println("Error in task skipped listener: " + e.getMessage());
            }
        });
    }
    
    @Override
    public void onTaskStopped(String taskId, String taskType) {
        listeners.forEach(listener -> {
            try {
                listener.onTaskStopped(taskId, taskType);
            } catch (Exception e) {
                System.err.println("Error in task stopped listener: " + e.getMessage());
            }
        });
    }
    
    public boolean isAutoReporting() {
        return autoReporting;
    }
    
    public int getReportIntervalSeconds() {
        return reportIntervalSeconds;
    }
    
    public void shutdown() {
        stopAutoReporting();
        try {
            scheduler.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}