package de.malkusch.ha.shared.infrastructure.scheduler;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
public final class Schedulers {

    public static void close(ScheduledExecutorService... schedulers) throws InterruptedException {
        for (var scheduler : schedulers) {
            try {
                close(scheduler);
            } catch (Exception e) {
                log.error("Shutting down scheduler {} failed", scheduler, e);
            }
        }
    }

    public static void close(ScheduledExecutorService scheduler) throws InterruptedException {
        scheduler.shutdown();
        if (scheduler.awaitTermination(10, SECONDS)) {
            return;
        }
        log.warn("Failed shutting down scheduler. Forcing shutdown now!");
        scheduler.shutdownNow();
        if (!scheduler.awaitTermination(10, SECONDS)) {
            log.error("Forced shutdown failed");
        }
    }

    public static ScheduledExecutorService singleThreadScheduler(String name) {
        return newSingleThreadScheduledExecutor(r -> {
            var thread = new Thread(r, name);
            thread.setUncaughtExceptionHandler((t, e) -> {
                log.error("Unhandled exception in {}", name, e);
            });
            thread.setDaemon(true);
            return thread;
        });
    }
}
