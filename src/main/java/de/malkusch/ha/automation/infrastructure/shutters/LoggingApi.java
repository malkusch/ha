package de.malkusch.ha.automation.infrastructure.shutters;

import static de.malkusch.ha.automation.model.shutters.Shutter.Api.State.CLOSED;

import de.malkusch.ha.automation.model.shutters.Shutter;
import de.malkusch.ha.automation.model.shutters.ShutterId;
import de.malkusch.ha.shared.model.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
final class LoggingApi implements Shutter.Api {

    private final ShutterId id;
    private volatile State state = CLOSED;

    @Override
    public State state() {
        return state;
    }

    @Override
    public void setState(State state) throws ApiException, InterruptedException {
        log.info("set {} to {}", id, state);
        this.state = state;
    }
}
