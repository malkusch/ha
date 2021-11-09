package de.malkusch.ha.automation.model.shutters;

import static de.malkusch.ha.automation.model.shutters.Shutter.Api.State.CLOSED;
import static de.malkusch.ha.automation.model.shutters.Shutter.Api.State.OPEN;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.time.Duration;

import de.malkusch.ha.automation.model.shutters.Shutter.Api.State;
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
        void setState(State state) throws ApiException, InterruptedException;

        State state() throws ApiException, InterruptedException;

        static record State(int percent) {

            public static final State OPEN = new State(0);
            public static final State CLOSED = new State(100);

            public State(int percent) {
                if (percent < 0 || percent > 100) {
                    throw new IllegalArgumentException("percent must be between 0 and 100");
                }
                this.percent = percent;
            }

            @Override
            public String toString() {
                return String.format("%d%%", percent);
            }
        }
    }

    public final void open() throws ApiException, InterruptedException {
        setState(OPEN);
    }

    public final void close() throws ApiException, InterruptedException {
        setState(CLOSED);
    }

    private void setState(State state) throws InterruptedException, ApiException {
        if (api.state().equals(state)) {
            return;
        }
        log.info("Closing shutter {} to {}", this, state);
        api.setState(state);
        MILLISECONDS.sleep(delay.toMillis());
    }

    @Override
    public final String toString() {
        return id.name();
    }
}
