package de.malkusch.ha.automation.model.scooter.charging;

import static de.malkusch.ha.shared.infrastructure.DateUtil.formatTime;
import static java.util.Collections.sort;
import static java.util.Collections.unmodifiableList;

import java.time.Duration;
import java.util.List;

import de.malkusch.ha.automation.infrastructure.scooter.ScooterEnabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.infrastructure.scooter.ScooterConfiguration.ScooterProperties;
import de.malkusch.ha.automation.model.Rule;
import de.malkusch.ha.automation.model.scooter.Scooter.ScooterException;
import de.malkusch.ha.automation.model.scooter.ScooterWallbox;
import de.malkusch.ha.automation.model.scooter.ScooterWallbox.WallboxException;
import de.malkusch.ha.automation.model.scooter.charging.ChargingStrategy.Evaluation;
import de.malkusch.ha.automation.model.scooter.charging.ChargingStrategy.Evaluation.Reason;
import de.malkusch.ha.automation.model.scooter.charging.ChargingStrategy.Evaluation.Request;
import de.malkusch.ha.automation.model.scooter.charging.ContextFactory.Context;
import de.malkusch.ha.shared.infrastructure.CoolDown.CoolDownException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@ScooterEnabled
public final class ChargingRule implements Rule {

    private final Duration evaluationRate;
    private final ContextFactory contextFactory;
    private final List<ChargingStrategy> priorizedStrategies;
    private final ScooterWallbox wallbox;

    @Autowired
    public ChargingRule(ScooterProperties properties, ContextFactory contextFactory, List<ChargingStrategy> strategies,
            ScooterWallbox wallbox) {

        this(properties.getChargingRule().getEvaluationRate(), contextFactory, priorized(strategies), wallbox);

        log.info("Using charging strategies in this order:");
        this.priorizedStrategies.stream().forEach(it -> log.info("- {}", it));
    }

    private static List<ChargingStrategy> priorized(List<ChargingStrategy> strategies) {
        sort(strategies);
        return unmodifiableList(strategies);
    }

    @Override
    public void evaluate() throws Exception {
        try {
            var context = contextFactory.context();

            var evaluation = select(priorizedStrategies, context);

            switch (evaluation.request()) {
            case START -> startCharging(evaluation.reason());
            case STOP -> stopCharging(evaluation.reason());
            default -> log.debug("Nothing to change");
            }

        } catch (WallboxException e) {
            log.warn("Ignore scooter charging automation: {}", e.error);

        } catch (ScooterException e) {
            log.warn("Ignore scooter charging automation: {}", e.error);

        } catch (CoolDownException e) {
            log.warn("Cooldown scooter charging automation until: {}", formatTime(e.retryAfter));
        }
    }

    private void startCharging(Reason reason) throws Exception {
        if (wallbox.isCharging()) {
            log.debug("Wallbox is already charging, new reason: {}", reason);
            return;
        }
        wallbox.startCharging();
        log.info("Wallbox started charging: {}", reason);
    }

    private void stopCharging(Reason reason) throws Exception {
        if (!wallbox.isCharging()) {
            log.debug("Wallbox has already stopped charging, new reason: {}", reason);
            return;
        }
        wallbox.stopCharging();
        log.info("Wallbox stoped charging: {}", reason);
    }

    @FunctionalInterface
    interface Change {
        void change() throws Exception;

        static void nothing() {
        }
    }

    private static Evaluation select(List<ChargingStrategy> priorizedStrategies, Context context) throws Exception {
        for (var strategy : priorizedStrategies) {
            log.debug("Evaluating {}", strategy);
            var evaluation = strategy.evaluate(context);
            if (evaluation.request() != Request.NONE) {
                log.debug("Selected {}: {}", strategy, evaluation);
                return evaluation;
            }
        }
        return Evaluation.NONE;
    }

    @Override
    public Duration evaluationRate() {
        return evaluationRate;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
