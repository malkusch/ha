package de.malkusch.ha.automation.infrastructure.theater.avr.denon;

import de.malkusch.ha.automation.infrastructure.theater.avr.AvrEventPublisher;
import io.theves.denon4j.controls.AbstractControl;
import io.theves.denon4j.net.Event;
import io.theves.denon4j.net.Protocol;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class PowerEventHandler extends AbstractControl {

    private final AvrEventPublisher publisher;

    public PowerEventHandler(Protocol protocol, AvrEventPublisher publisher) {
        super("PW", protocol);
        this.publisher = publisher;
    }

    @Override
    protected void doHandle(Event event) {
        switch (event.getParameter().getValue()) {
        case "STANDBY", "OFF" -> publisher.publishTurnedOff();
        case "ON" -> publisher.publishTurnedOn();
        default -> log.warn("Unhandled AVR Power Event: {}", event);
        }
    }

    @Override
    protected void doInit() {
    }
}