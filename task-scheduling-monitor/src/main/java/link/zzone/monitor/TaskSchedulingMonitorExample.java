package link.zzone.monitor;

import io.netty.util.Timeout;
import link.zzone.server.timer.WheelTimer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.CountDownLatch;
import java.util.Random;

/**
 * @author chrischen
 * @date 2025/8/7
 * @desc Example demonstrating task scheduling monitor usage
 */
public class TaskSchedulingMonitorExample {
    
    public static void main(String[] args) throws InterruptedException {
        // Initialize monitoring system
        TaskSchedulingMonitorManager monitorManager = new TaskSchedulingMonitorManager();
        
        // Start auto-reporting every 10 seconds
        monitorManager.startAutoReporting(10);
        
        // Initialize wheel timer (in a real Spring app, this would be autowired)
        WheelTimer wheelTimer = new WheelTimer();
        wheelTimer.init();
        
        System.out.println("Starting task scheduling monitor example...");
        System.out.println("Monitor will report every 10 seconds");
        
        // Simulate various task scenarios
        simulateTaskExecution(monitorManager, wheelTimer);
        
        // Wait a bit for tasks to complete and reports to be generated
        Thread.sleep(30000);
        
        // Generate final detailed report
        System.out.println("\n=== Final Detailed Report ===");
        monitorManager.printDetailedReport();
        
        // Clean up
        monitorManager.shutdown();
        wheelTimer.destroy();
    }
    
    private static void simulateTaskExecution(TaskSchedulingMonitorManager monitorManager, WheelTimer wheelTimer) {
        Random random = new Random();
        CountDownLatch latch = new CountDownLatch(50);
        
        // Submit various types of tasks
        for (int i = 0; i < 50; i++) {
            final int taskNum = i;
            String taskType = "type-" + (i % 5); // 5 different task types
            
            if (i % 10 == 9) {
                // Some tasks that will fail
                wheelTimer.scheduleTask(timeout -> {
                    Thread.sleep(100 + random.nextInt(200));
                    latch.countDown();
                    throw new RuntimeException("Simulated failure for task " + taskNum);
                }, random.nextInt(5), TimeUnit.SECONDS, taskType);
            } else if (i % 15 == 14) {
                // Some tasks to skip (simulate by calling skip directly)
                String taskId = "manual-task-" + taskNum;
                monitorManager.onTaskSubmitted(taskId, taskType);
                monitorManager.onTaskSkipped(taskId, taskType);
                latch.countDown();
            } else {
                // Normal successful tasks
                wheelTimer.scheduleTask(timeout -> {
                    // Simulate work with varying execution times
                    Thread.sleep(50 + random.nextInt(500));
                    latch.countDown();
                    System.out.println("Completed task " + taskNum + " of type " + taskType);
                }, random.nextInt(5), TimeUnit.SECONDS, taskType);
            }
        }
        
        // Also demonstrate the MonitoredTimerTask
        for (int i = 0; i < 10; i++) {
            final int taskNum = 50 + i;
            wheelTimer.scheduleMonitoredTask(new WheelTimer.MonitoredTimerTask("monitored-" + taskNum, "monitored-type") {
                @Override
                public void run(Timeout timeout) throws Exception {
                    Thread.sleep(100 + random.nextInt(300));
                    System.out.println("Completed monitored task " + taskNum);
                }
            }, random.nextInt(3), TimeUnit.SECONDS);
        }
        
        System.out.println("Submitted 60 tasks for execution...");
    }
}