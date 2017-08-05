package com.github.hronom.prioritized.scheduling.example;

import java.util.Comparator;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class TasksManager implements AutoCloseable {
    private final int maxUsedThreads = 2;

    private final ScheduledExecutorService scheduledExecutorService =
        Executors.newSingleThreadScheduledExecutor();
    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
        maxUsedThreads,
        Integer.MAX_VALUE,
        0,
        NANOSECONDS,
        new PriorityBlockingQueue<>(30, Comparator.comparing(o -> ((PrioritizedTask) o)))
    );

    @Override
    public void close() throws Exception {
        scheduledExecutorService.shutdown();
        threadPoolExecutor.shutdown();

        scheduledExecutorService.awaitTermination(5, TimeUnit.MINUTES);
        threadPoolExecutor.awaitTermination(5, TimeUnit.MINUTES);
    }

    public PrioritizedTask schedule(Runnable command, int priority, long delay, TimeUnit unit) {
        PrioritizedTask prioritizedTask = new PrioritizedTask(command, priority);
        scheduledExecutorService.schedule(() -> threadPoolExecutor.execute(prioritizedTask), delay, unit);
        return prioritizedTask;
    }
}
