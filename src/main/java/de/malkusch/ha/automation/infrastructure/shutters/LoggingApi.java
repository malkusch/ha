package de.malkusch.ha.automation.infrastructure.shutters;

import static de.malkusch.ha.automation.model.shutters.Shutter.Api.State.CLOSED;

import de.malkusch.ha.automation.model.shutters.Shutter;
import de.malkusch.ha.automation.model.shutters.ShutterId;
import de.malkusch.ha.shared.model.ApiException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class LoggingApi implements Shutter.Api {

    private volatile State state = CLOSED;

    @Override
    public State state(ShutterId id) {
        return state;
    }

    @Override
    public void setState(ShutterId id, State state) throws ApiException, InterruptedException {
        log.info("set {} to {}", id, state);
        this.state = state;
    }
}
