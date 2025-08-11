package link.zzone.server.timer;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import io.netty.util.concurrent.DefaultThreadFactory;
import link.zzone.monitor.TaskSchedulingMonitorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author chrischen
 * @date 2025/6/26
 * @desc WheelTimer with monitoring support
 */
@Component
public class WheelTimer {
    
    private HashedWheelTimer timer;
    private ExecutorService workerExecutor;
    private final AtomicLong taskIdGenerator = new AtomicLong(0);
    
    @Autowired(required = false)
    private TaskSchedulingMonitorManager monitorManager;
    
    @PostConstruct
    public void init() {
        this.workerExecutor = Executors.newFixedThreadPool(10, 
            new DefaultThreadFactory("wheel-timer-worker"));
        
        this.timer = new HashedWheelTimer(
            new DefaultThreadFactory("wheel-timer"),
            1, TimeUnit.SECONDS,
            60, true, -1,
            this.workerExecutor
        );
    }
    
    public Timeout scheduleTask(TimerTask task, long delay, TimeUnit unit) {
        return scheduleTask(task, delay, unit, "default");
    }
    
    public Timeout scheduleTask(TimerTask task, long delay, TimeUnit unit, String taskType) {
        String taskId = "task-" + taskIdGenerator.incrementAndGet();
        
        if (monitorManager != null) {
            monitorManager.onTaskSubmitted(taskId, taskType);
        }
        
        return timer.newTimeout(timeout -> {
            if (monitorManager != null) {
                monitorManager.onTaskStarted(taskId, taskType);
            }
            
            try {
                task.run(timeout);
                if (monitorManager != null) {
                    monitorManager.onTaskCompleted(taskId, taskType);
                }
            } catch (Exception e) {
                if (monitorManager != null) {
                    monitorManager.onTaskFailed(taskId, taskType, e);
                }
                throw e;
            }
        }, delay, unit);
    }
    
    public Timeout scheduleMonitoredTask(MonitoredTimerTask task, long delay, TimeUnit unit) {
        String taskId = task.getTaskId();
        String taskType = task.getTaskType();
        
        if (monitorManager != null) {
            monitorManager.onTaskSubmitted(taskId, taskType);
        }
        
        return timer.newTimeout(timeout -> {
            if (monitorManager != null) {
                monitorManager.onTaskStarted(taskId, taskType);
            }
            
            try {
                task.run(timeout);
                if (monitorManager != null) {
                    monitorManager.onTaskCompleted(taskId, taskType);
                }
            } catch (Exception e) {
                if (monitorManager != null) {
                    monitorManager.onTaskFailed(taskId, taskType, e);
                }
                throw e;
            }
        }, delay, unit);
    }
    
    @PreDestroy
    public void destroy() {
        if (timer != null) {
            timer.stop();
        }
        if (workerExecutor != null) {
            workerExecutor.shutdown();
            try {
                workerExecutor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    public static abstract class MonitoredTimerTask implements TimerTask {
        private final String taskId;
        private final String taskType;
        
        public MonitoredTimerTask(String taskId, String taskType) {
            this.taskId = taskId;
            this.taskType = taskType;
        }
        
        public String getTaskId() {
            return taskId;
        }
        
        public String getTaskType() {
            return taskType;
        }
    }
}
