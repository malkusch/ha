package de.malkusch.ha.automation.infrastructure.rule;

import static de.malkusch.ha.shared.model.event.EventPublisher.publish;
import static java.time.Duration.ZERO;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.model.Rule;
import de.malkusch.ha.shared.model.event.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public final class RuleScheduler implements AutoCloseable {

    private final ScheduledExecutorService scheduler = newSingleThreadScheduledExecutor();

    RuleScheduler(@Value("${scheduler.delay}") Duration delay) {
        this.delay = delay;
    }

    private final Duration delay;
    private Duration nextDelay = ZERO;

    public void schedule(Rule rule) {
        log.info("Scheduling {} in {}", rule, nextDelay);
        var rateInSeconds = rule.evaluationRate().toSeconds();
        scheduler.scheduleAtFixedRate(() -> evaluate(rule), nextDelay.toSeconds(), rateInSeconds, SECONDS);
        nextDelay = nextDelay.plus(delay);
    }

    @RequiredArgsConstructor
    public static final class RuleEvaluationFailed implements Event {
        public final String message;
    }

    private void evaluate(Rule rule) {
        try {
            log.debug("Evaluating {}", rule);
            rule.evaluate();

        } catch (Exception e) {
            var reference = UUID.randomUUID().toString();
            var message = String.format("Failed to evaluate %s [%s]", rule, reference);
            log.error(message, e);
            publish(new RuleEvaluationFailed(message));
        }
    }

    @Override
    public void close() throws InterruptedException {
        scheduler.shutdown();
        if (scheduler.awaitTermination(10, SECONDS)) {
            return;
        }
        log.warn("Failed shutting down scheduler. Forcing shutdown now!");
        scheduler.shutdownNow();
        if (!scheduler.awaitTermination(10, SECONDS)) {
            log.error("Forced shutdown failed");
        }
    }
}
