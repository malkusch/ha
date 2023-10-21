package de.malkusch.ha.automation.infrastructure.theater.avr.denon;

import de.malkusch.ha.automation.model.theater.AvrTurnedOff;
import de.malkusch.ha.automation.model.theater.AvrTurnedOn;
import io.theves.denon4j.controls.AbstractControl;
import io.theves.denon4j.net.Event;
import io.theves.denon4j.net.Protocol;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class PowerEvent extends AbstractControl {

    private final EventPublisher publisher;

    public PowerEvent(Protocol protocol, EventPublisher publisher) {
        super("PW", protocol);
        this.publisher = publisher;
    }

    @Override
    protected void doHandle(Event event) {
        switch (event.getParameter().getValue()) {
        case "STANDBY", "OFF" -> publisher.publish(new AvrTurnedOff());
        case "ON" -> publisher.publish(new AvrTurnedOn());
        default -> log.warn("Unhandled AVR Power Event: {}", event);
        }
    }

    @Override
    protected void doInit() {
    }
}