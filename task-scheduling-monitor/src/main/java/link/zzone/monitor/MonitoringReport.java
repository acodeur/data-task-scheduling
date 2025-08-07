package link.zzone.monitor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author chrischen
 * @date 2025/8/7
 * @desc Monitoring report containing all task scheduling metrics
 */
public class MonitoringReport {
    
    private final long totalTasksSubmitted;
    private final long totalTasksCompleted;
    private final long totalTasksFailed;
    private final long totalTasksSkipped;
    private final long currentRunningTasks;
    private final long currentWaitingTasks;
    
    private final double successRate;
    private final double failureRate;
    private final long averageExecutionTime;
    private final long maxExecutionTime;
    private final long minExecutionTime;
    
    private final List<TaskMetrics> taskTypeMetrics;
    private final LocalDateTime monitoringStartTime;
    private final LocalDateTime reportGenerationTime;
    
    public MonitoringReport(
            long totalTasksSubmitted, long totalTasksCompleted, long totalTasksFailed,
            long totalTasksSkipped, long currentRunningTasks, long currentWaitingTasks,
            double successRate, double failureRate, long averageExecutionTime,
            long maxExecutionTime, long minExecutionTime, List<TaskMetrics> taskTypeMetrics,
            LocalDateTime monitoringStartTime, LocalDateTime reportGenerationTime) {
        
        this.totalTasksSubmitted = totalTasksSubmitted;
        this.totalTasksCompleted = totalTasksCompleted;
        this.totalTasksFailed = totalTasksFailed;
        this.totalTasksSkipped = totalTasksSkipped;
        this.currentRunningTasks = currentRunningTasks;
        this.currentWaitingTasks = currentWaitingTasks;
        this.successRate = successRate;
        this.failureRate = failureRate;
        this.averageExecutionTime = averageExecutionTime;
        this.maxExecutionTime = maxExecutionTime;
        this.minExecutionTime = minExecutionTime;
        this.taskTypeMetrics = taskTypeMetrics;
        this.monitoringStartTime = monitoringStartTime;
        this.reportGenerationTime = reportGenerationTime;
    }
    
    public String generateDetailedReport() {
        StringBuilder report = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        report.append("=== Task Scheduling Monitor Report ===\n");
        report.append("Report Generated: ").append(reportGenerationTime.format(formatter)).append("\n");
        report.append("Monitoring Since: ").append(monitoringStartTime.format(formatter)).append("\n\n");
        
        // Overall Statistics
        report.append("=== Overall Statistics ===\n");
        report.append("Total Tasks Submitted: ").append(totalTasksSubmitted).append("\n");
        report.append("Total Tasks Completed: ").append(totalTasksCompleted).append("\n");
        report.append("Total Tasks Failed: ").append(totalTasksFailed).append("\n");
        report.append("Total Tasks Skipped: ").append(totalTasksSkipped).append("\n");
        report.append("Current Running Tasks: ").append(currentRunningTasks).append("\n");
        report.append("Current Waiting Tasks: ").append(currentWaitingTasks).append("\n");
        report.append("Success Rate: ").append(String.format("%.2f%%", successRate)).append("\n");
        report.append("Failure Rate: ").append(String.format("%.2f%%", failureRate)).append("\n\n");
        
        // Performance Metrics
        report.append("=== Performance Metrics ===\n");
        report.append("Average Execution Time: ").append(averageExecutionTime).append("ms\n");
        report.append("Max Execution Time: ").append(maxExecutionTime).append("ms\n");
        report.append("Min Execution Time: ").append(minExecutionTime).append("ms\n\n");
        
        // Task Type Breakdown
        if (!taskTypeMetrics.isEmpty()) {
            report.append("=== Task Type Breakdown ===\n");
            for (TaskMetrics metrics : taskTypeMetrics) {
                report.append("Task Type: ").append(metrics.getTaskType()).append("\n");
                report.append("  Submitted: ").append(metrics.getSubmitted()).append("\n");
                report.append("  Completed: ").append(metrics.getCompleted()).append("\n");
                report.append("  Failed: ").append(metrics.getFailed()).append("\n");
                report.append("  Skipped: ").append(metrics.getSkipped()).append("\n");
                report.append("  Success Rate: ").append(String.format("%.2f%%", metrics.getSuccessRate())).append("\n");
                report.append("  Avg Execution Time: ").append(metrics.getAverageExecutionTime()).append("ms\n");
                
                List<String> recentErrors = metrics.getRecentErrors();
                if (!recentErrors.isEmpty()) {
                    report.append("  Recent Errors:\n");
                    for (String error : recentErrors) {
                        report.append("    ").append(error).append("\n");
                    }
                }
                report.append("\n");
            }
        }
        
        return report.toString();
    }
    
    public String generateSummaryReport() {
        return String.format(
            "Tasks: %d submitted, %d completed, %d failed (%.2f%% success), %d running, %d waiting",
            totalTasksSubmitted, totalTasksCompleted, totalTasksFailed, 
            successRate, currentRunningTasks, currentWaitingTasks
        );
    }
    
    // Getters
    public long getTotalTasksSubmitted() { return totalTasksSubmitted; }
    public long getTotalTasksCompleted() { return totalTasksCompleted; }
    public long getTotalTasksFailed() { return totalTasksFailed; }
    public long getTotalTasksSkipped() { return totalTasksSkipped; }
    public long getCurrentRunningTasks() { return currentRunningTasks; }
    public long getCurrentWaitingTasks() { return currentWaitingTasks; }
    public double getSuccessRate() { return successRate; }
    public double getFailureRate() { return failureRate; }
    public long getAverageExecutionTime() { return averageExecutionTime; }
    public long getMaxExecutionTime() { return maxExecutionTime; }
    public long getMinExecutionTime() { return minExecutionTime; }
    public List<TaskMetrics> getTaskTypeMetrics() { return taskTypeMetrics; }
    public LocalDateTime getMonitoringStartTime() { return monitoringStartTime; }
    public LocalDateTime getReportGenerationTime() { return reportGenerationTime; }
}