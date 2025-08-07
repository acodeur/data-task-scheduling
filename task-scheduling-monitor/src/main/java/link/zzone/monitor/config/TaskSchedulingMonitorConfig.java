package link.zzone.monitor.config;

import link.zzone.monitor.TaskSchedulingMonitor;
import link.zzone.monitor.TaskSchedulingMonitorManager;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author chrischen
 * @date 2025/8/7
 * @desc Spring configuration for task scheduling monitor
 */
@Configuration
@ConfigurationProperties(prefix = "task.scheduling.monitor")
public class TaskSchedulingMonitorConfig {
    
    private boolean enabled = true;
    private boolean autoReporting = false;
    private int reportIntervalSeconds = 60;
    
    @Bean
    public TaskSchedulingMonitor taskSchedulingMonitor() {
        TaskSchedulingMonitor monitor = new TaskSchedulingMonitor();
        if (!enabled) {
            monitor.disableMonitoring();
        }
        return monitor;
    }
    
    @Bean
    public TaskSchedulingMonitorManager taskSchedulingMonitorManager(TaskSchedulingMonitor monitor) {
        TaskSchedulingMonitorManager manager = new TaskSchedulingMonitorManager(monitor);
        
        if (enabled && autoReporting) {
            manager.startAutoReporting(reportIntervalSeconds);
        }
        
        return manager;
    }
    
    // Getters and Setters for configuration properties
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public boolean isAutoReporting() {
        return autoReporting;
    }
    
    public void setAutoReporting(boolean autoReporting) {
        this.autoReporting = autoReporting;
    }
    
    public int getReportIntervalSeconds() {
        return reportIntervalSeconds;
    }
    
    public void setReportIntervalSeconds(int reportIntervalSeconds) {
        this.reportIntervalSeconds = reportIntervalSeconds;
    }
}