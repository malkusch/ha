package de.malkusch.ha.shared.infrastructure.event;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DeferredEventPublisherTest {

    @Mock
    private EventPublisher publisher;

    private final DeferredEventPublisher deferred = new DeferredEventPublisher();

    private static enum AnyEvent implements Event {
        A, B;
    }

    @Test
    void publishShouldDefer() {
        deferred.publish(AnyEvent.A);

        verify(publisher, never()).publishSafely(any());
    }

    @Test
    void publishSafelyShouldDefer() {
        deferred.publishSafely(AnyEvent.A);

        verify(publisher, never()).publishSafely(any());
    }

    @Test
    void forwardShouldPublishDeferred() {
        deferred.publish(AnyEvent.A);
        deferred.publishSafely(AnyEvent.B);

        deferred.forward(publisher);

        var inOrder = inOrder(publisher);
        inOrder.verify(publisher).publishSafely(AnyEvent.A);
        inOrder.verify(publisher).publishSafely(AnyEvent.B);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void secondForwardShouldFail() {
        deferred.forward(publisher);

        assertThrows(IllegalStateException.class, () -> deferred.forward(publisher));
    }

    @Test
    void publishShouldForward() {
        deferred.forward(publisher);

        deferred.publish(AnyEvent.A);

        verify(publisher).publish(AnyEvent.A);
    }

    @Test
    void publishSafelyShouldForward() {
        deferred.forward(publisher);

        deferred.publishSafely(AnyEvent.A);

        verify(publisher).publishSafely(AnyEvent.A);
    }
}
