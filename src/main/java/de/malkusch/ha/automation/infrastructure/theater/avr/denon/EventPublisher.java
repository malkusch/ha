package de.malkusch.ha.automation.infrastructure.theater.avr.denon;

import static de.malkusch.ha.shared.infrastructure.event.EventPublisher.publishSafely;
import static de.malkusch.ha.shared.infrastructure.scheduler.Schedulers.singleThreadScheduler;

import java.util.concurrent.ScheduledExecutorService;

import org.springframework.stereotype.Service;

import de.malkusch.ha.shared.infrastructure.event.Event;
import de.malkusch.ha.shared.infrastructure.scheduler.Schedulers;

@Service
final class EventPublisher implements AutoCloseable {

    private final ScheduledExecutorService event_thread = singleThreadScheduler("denon-event");

    public void publish(Event event) {
        event_thread.execute(() -> publishSafely(event));
    }

    @Override
    public void close() throws Exception {
        Schedulers.close(event_thread);
    }
}
