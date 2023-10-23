package de.malkusch.ha.automation.infrastructure.theater.avr;

import de.malkusch.ha.automation.model.theater.AvrTurnedOff;
import de.malkusch.ha.automation.model.theater.AvrTurnedOn;
import de.malkusch.ha.shared.infrastructure.event.EventPublisher;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class AvrEventPublisher implements AutoCloseable {

    private final EventPublisher publisher;

    public void publishTurnedOn() {
        publisher.publishSafely(new AvrTurnedOn());
    }

    public void publishTurnedOff() {
        publisher.publishSafely(new AvrTurnedOff());
    }

    @Override
    public void close() throws Exception {
        publisher.close();
    }
}
