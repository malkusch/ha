package de.malkusch.ha.shared.infrastructure.event;

public final class StaticEventPublisher {

    final static class BootEventPublisher implements EventPublisher {

        private volatile EventPublisher publisher = new DeferredEventPublisher();

        void setup(EventPublisher eventPublisher) throws Exception {
            if (publisher instanceof DeferredEventPublisher deferred) {
                try (deferred) {
                    deferred.forward(eventPublisher);
                } finally {
                    publisher = eventPublisher;
                }
            } else {
                throw new IllegalStateException("Boot publisher was already setup");
            }
        }

        public void publish(Event event) {
            publisher.publish(event);
        }

        public void publishSafely(Event event) {
            publisher.publishSafely(event);
        }

        @Override
        public String toString() {
            return publisher.toString();
        }

        @Override
        public void close() throws Exception {
            try (var old = publisher) {
                publisher = new DeferredEventPublisher();
            }
        }
    }

    static final BootEventPublisher PUBLISHER = new BootEventPublisher();

    public static void publish(Event event) {
        PUBLISHER.publish(event);
    }

    public static void publishSafely(Event event) {
        PUBLISHER.publishSafely(event);
    }
}
