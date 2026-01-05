package de.malkusch.ha.automation.infrastructure.rule;

import de.malkusch.ha.automation.model.Rule;
import de.malkusch.ha.shared.infrastructure.circuitbreaker.CircuitBreakerConfiguration;
import de.malkusch.ha.shared.infrastructure.circuitbreaker.CircuitBreakerFactory;
import de.malkusch.ha.shared.infrastructure.circuitbreaker.VoidCircuitBreaker;
import de.malkusch.ha.shared.infrastructure.event.Event;
import de.malkusch.ha.shared.infrastructure.scheduler.Schedulers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;

import static de.malkusch.ha.shared.infrastructure.DateUtil.formatDuration;
import static de.malkusch.ha.shared.infrastructure.event.StaticEventPublisher.publishSafely;
import static java.time.Duration.ZERO;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.SECONDS;

@Service
@Slf4j
public final class RuleScheduler implements AutoCloseable {

    private final ScheduledExecutorService scheduler = newSingleThreadScheduledExecutor(r -> {
        var thread = new Thread(r, "RuleScheduler");
        thread.setUncaughtExceptionHandler((t, e) -> {
            log.error("Unhandled error in RuleScheduler", e);
        });
        thread.setDaemon(true);
        return thread;
    });

    @ConfigurationProperties("scheduler")
    record Configuration(Duration delay, CircuitBreakerConfiguration circuitBreaker) {
    }

    RuleScheduler(Configuration configuration, CircuitBreakerFactory circuitBreakerFactory) {
        this.delay = configuration.delay;
        this.circuitBreakerConfiguration = configuration.circuitBreaker;
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    private final Duration delay;
    private final CircuitBreakerConfiguration circuitBreakerConfiguration;
    private final CircuitBreakerFactory circuitBreakerFactory;
    private Duration nextDelay = ZERO;

    public void schedule(Rule rule) {
        nextDelay = nextDelay.plus(delay);
        log.info("Scheduling {} in {}", rule, formatDuration(nextDelay));
        var rateInSeconds = rule.evaluationRate().toSeconds();
        var withCircuitBreaker = new CircuitBreakerRule(rule, circuitBreakerFactory.buildSilentCircuitBreaker(rule.toString(), circuitBreakerConfiguration));
        scheduler.scheduleAtFixedRate(() -> evaluate(withCircuitBreaker), nextDelay.toSeconds(), rateInSeconds, SECONDS);
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
