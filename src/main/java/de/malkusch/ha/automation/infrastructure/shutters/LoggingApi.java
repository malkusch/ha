package de.malkusch.ha.automation.infrastructure.shutters;

import de.malkusch.ha.automation.model.shutters.Shutter;
import de.malkusch.ha.automation.model.shutters.ShutterId;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class LoggingApi implements Shutter.Api {

    private volatile State state = State.CLOSED;

    @Override
    public void open(ShutterId id) {
        log.info("Opening {}", id);
        state = State.OPEN;
    }

    @Override
    public void close(ShutterId id) {
        log.info("Closing {}", id);
        state = State.CLOSED;
    }

    @Override
    public State state(ShutterId id) {
        return state;
    }
}
