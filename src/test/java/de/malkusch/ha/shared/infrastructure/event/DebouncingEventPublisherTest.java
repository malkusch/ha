package de.malkusch.ha.shared.infrastructure.event;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Duration;

import org.junit.jupiter.api.Test;

public class DebouncingEventPublisherTest {

    private static final Duration ANY_DEBOUNCE_INTERVAL = Duration.ofMillis(500);
    private final EventPublisher publisher = mock(EventPublisher.class);
    private final EventPublisher debounced = new DebouncingEventPublisher(publisher, ANY_DEBOUNCE_INTERVAL);

    private static enum AnyEvent implements Event {
        A, B;
    }

    @Test
    void publishShouldDebounceSameEvent() {
        debounced.publish(AnyEvent.A);

        debounced.publish(AnyEvent.A);

        verify(publisher).publish(AnyEvent.A);
    }

    @Test
    void publishSafelyShouldDebounceSameEvent() {
        debounced.publishSafely(AnyEvent.A);

        debounced.publishSafely(AnyEvent.A);

        verify(publisher).publishSafely(AnyEvent.A);
    }

    @Test
    void publishShouldNotDebounceSameEventAfterWhile() throws InterruptedException {
        debounced.publish(AnyEvent.A);
        MILLISECONDS.sleep(ANY_DEBOUNCE_INTERVAL.plusMillis(50).toMillis());

        debounced.publish(AnyEvent.A);

        verify(publisher, times(2)).publish(AnyEvent.A);
    }

    @Test
    void publishSafelyShouldNotDebounceSameEventAfterWhile() throws InterruptedException {
        debounced.publishSafely(AnyEvent.A);
        MILLISECONDS.sleep(ANY_DEBOUNCE_INTERVAL.plusMillis(50).toMillis());

        debounced.publishSafely(AnyEvent.A);

        verify(publisher, times(2)).publishSafely(AnyEvent.A);
    }

    @Test
    void publishShouldNotDebounceOtherEvent() {
        debounced.publish(AnyEvent.A);
        debounced.publish(AnyEvent.B);
        debounced.publish(AnyEvent.A);
        debounced.publish(AnyEvent.B);

        var inOrder = inOrder(publisher);
        inOrder.verify(publisher).publish(AnyEvent.A);
        inOrder.verify(publisher).publish(AnyEvent.B);
        inOrder.verify(publisher).publish(AnyEvent.A);
        inOrder.verify(publisher).publish(AnyEvent.B);
    }

    @Test
    void publishSafelyShouldNotDebounceOtherEvent() {
        debounced.publishSafely(AnyEvent.A);
        debounced.publishSafely(AnyEvent.B);
        debounced.publishSafely(AnyEvent.A);
        debounced.publishSafely(AnyEvent.B);

        var inOrder = inOrder(publisher);
        inOrder.verify(publisher).publishSafely(AnyEvent.A);
        inOrder.verify(publisher).publishSafely(AnyEvent.B);
        inOrder.verify(publisher).publishSafely(AnyEvent.A);
        inOrder.verify(publisher).publishSafely(AnyEvent.B);
    }
}
