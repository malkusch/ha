package de.malkusch.ha.automation.infrastructure.rule;

import static de.malkusch.ha.shared.infrastructure.event.EventPublisher.publishSafely;
import static java.lang.System.exit;
import static java.time.Duration.ZERO;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.model.Rule;
import de.malkusch.ha.shared.infrastructure.event.Event;
import de.malkusch.ha.shared.infrastructure.scheduler.Schedulers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public final class RuleScheduler implements AutoCloseable {

    private final ScheduledExecutorService scheduler = newSingleThreadScheduledExecutor(r -> {
        var thread = new Thread(r, "RuleScheduler");
        thread.setUncaughtExceptionHandler((t, e) -> {
            log.error("Shutting down due to an error in RuleScheduler", e);
            exit(-1);
        });
        thread.setDaemon(true);
        return thread;
    });

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
        public final String rule;
        public final String cause;
        public final String reference;
    }

    private void evaluate(Rule rule) {
        try {
            log.debug("Evaluating {}", rule);
            rule.evaluate();

        } catch (Exception e) {
            var reference = randomUUID();
            log.error("{} failed [{}]", rule, reference, e);
            publishSafely(new RuleEvaluationFailed(rule.toString(), e.getMessage(), reference.toString()));
        }
    }

    @Override
    public void close() throws InterruptedException {
        Schedulers.close(scheduler);
    }
}
