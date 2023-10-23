package de.malkusch.ha.shared.infrastructure.event;

import static de.malkusch.ha.shared.infrastructure.scheduler.Schedulers.singleThreadScheduler;

import java.util.concurrent.ScheduledExecutorService;

import de.malkusch.ha.shared.infrastructure.scheduler.Schedulers;

public final class AsyncEventPublisher implements EventPublisher {

    private final ScheduledExecutorService event_thread;
    private final EventPublisher publisher;

    public AsyncEventPublisher(String name, EventPublisher publisher) {
        event_thread = singleThreadScheduler(name);
        this.publisher = publisher;
    }

    @Override
    public void publish(Event event) {
        publishSafely(event);
    }

    @Override
    public void publishSafely(Event event) {
        event_thread.execute(() -> publisher.publishSafely(event));
    }

    @Override
    public void close() throws Exception {
        try (publisher) {
            Schedulers.close(event_thread);
        }
    }

    @Override
    public String toString() {
        return event_thread.toString();
    }
}
