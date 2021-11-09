package de.malkusch.ha.automation.model.shutters;

import static de.malkusch.ha.automation.model.shutters.Shutter.Api.State.CLOSED;
import static de.malkusch.ha.automation.model.shutters.Shutter.Api.State.OPEN;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.time.Duration;

import de.malkusch.ha.shared.model.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class Shutter {

    public final ShutterId id;
    private final Api api;
    private final Duration delay;

    public static interface Api {
        void open(ShutterId id) throws ApiException, InterruptedException;

        void close(ShutterId id) throws ApiException, InterruptedException;

        State state(ShutterId id) throws ApiException, InterruptedException;

        static enum State {
            OPEN, HALF_CLOSED, CLOSED
        }
    }

    public final void open() throws ApiException, InterruptedException {
        if (api.state(id) == OPEN) {
            return;
        }
        log.info("Opening shutter {}", this);
        api.open(id);
        delay();
    }

    public final void close() throws ApiException, InterruptedException {
        if (api.state(id) == CLOSED) {
            return;
        }
        log.info("Closing shutter {}", this);
        api.close(id);
        delay();
    }

    private void delay() throws InterruptedException {
        MILLISECONDS.sleep(delay.toMillis());
    }

    @Override
    public final String toString() {
        return id.name();
    }
}
