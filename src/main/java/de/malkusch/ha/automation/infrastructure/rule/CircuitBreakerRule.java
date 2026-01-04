package de.malkusch.ha.automation.infrastructure.rule;

import de.malkusch.ha.automation.model.Rule;
import de.malkusch.ha.shared.infrastructure.circuitbreaker.VoidCircuitBreaker;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

@RequiredArgsConstructor
final class CircuitBreakerRule implements Rule {

    private final Rule rule;
    private final VoidCircuitBreaker circuitBreaker;

    @Override
    public void evaluate() throws Exception {
        circuitBreaker.run(rule::evaluate);
    }

    @Override
    public Duration evaluationRate() {
        return rule.evaluationRate();
    }

    @Override
    public String toString() {
        return rule.toString();
    }
}
