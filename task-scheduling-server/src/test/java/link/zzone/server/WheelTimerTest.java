package link.zzone.server;

import io.netty.util.HashedWheelTimer;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author chrischen
 * @date 2025/7/12
 * @desc WheelTimerTest
 */
public class WheelTimerTest {

    public static void main(String[] args) {
        ExecutorService workerExecutorService = Executors.newFixedThreadPool(10);
        HashedWheelTimer timer = new HashedWheelTimer(
                new DefaultThreadFactory("test"),
                1,
                TimeUnit.SECONDS,
                60,
                true,
                -1,
                workerExecutorService
        );

        timer.newTimeout(timeout -> {
            Thread.sleep(2000);
            System.out.println(System.currentTimeMillis());
        }, 1, TimeUnit.SECONDS);
        timer.newTimeout(timeout -> {
            Thread.sleep(2000);
            System.out.println(System.currentTimeMillis());
        }, 1, TimeUnit.SECONDS);
    }
}
