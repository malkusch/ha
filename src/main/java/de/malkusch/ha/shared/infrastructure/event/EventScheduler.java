package de.malkusch.ha.shared.infrastructure.event;

import static de.malkusch.ha.shared.infrastructure.DateUtil.formatSeconds;
import static de.malkusch.ha.shared.infrastructure.DateUtil.formatTime;
import static de.malkusch.ha.shared.infrastructure.scheduler.Schedulers.singleThreadScheduler;
import static java.time.temporal.ChronoUnit.SECONDS;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import de.malkusch.ha.shared.infrastructure.scheduler.Schedulers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public final class EventScheduler implements AutoCloseable {

    private final EventPublisher publisher;
    private final ScheduledExecutorService scheduler = singleThreadScheduler("event-scheduler");

    private final Map<Class<? extends Event>, Collection<ScheduledFuture<?>>> events = new ConcurrentHashMap<>();

    public void cancel(Class<? extends Event> type) {
        var futures = events.remove(type);
        if (futures != null) {
            futures.forEach(it -> it.cancel(false));
        }
    }

    public void publishAt(Event event, LocalTime time) {
        var now = LocalDateTime.now();
        var scheduledDateTime = now.toLocalDate().atTime(time);
        if (scheduledDateTime.isBefore(now)) {
            scheduledDateTime = scheduledDateTime.plusDays(1);
        }
        var seconds = now.until(scheduledDateTime, SECONDS);

        log.info("Scheduling {} at {} ({})", event, formatTime(time), formatSeconds(seconds));
        var future = scheduler.schedule(() -> publisher.publishSafely(event), seconds, TimeUnit.SECONDS);
        var bucket = events.computeIfAbsent(event.getClass(), it -> new ConcurrentLinkedQueue<>());
        bucket.add(future);
    }

    @Scheduled(fixedRateString = "PT1h")
    public void clean() {
        events.values().stream().forEach(bucket -> bucket.removeIf(Future::isDone));
    }

    @Override
    public void close() throws Exception {
        events.values().stream().flatMap(Collection::stream).forEach(it -> it.cancel(true));
        Schedulers.close(scheduler);
    }
}
