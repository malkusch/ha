package de.malkusch.ha.automation.model.scooter.charging;

import static de.malkusch.ha.automation.model.electricity.Electricity.Aggregation.P75;
import static de.malkusch.ha.automation.model.electricity.Watt.min;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.infrastructure.LazyValue;
import de.malkusch.ha.automation.infrastructure.scooter.ScooterConfiguration.ScooterProperties;
import de.malkusch.ha.automation.model.electricity.Capacity;
import de.malkusch.ha.automation.model.electricity.Electricity;
import de.malkusch.ha.automation.model.electricity.Watt;
import de.malkusch.ha.automation.model.scooter.Scooter;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
final class ContextFactory {

    private final Scooter scooter;
    private final Electricity electricity;
    private final Duration excessWindow;

    @Autowired
    public ContextFactory(Scooter scooter, Electricity electricity, ScooterProperties properties) {
        this(scooter, electricity, properties.getChargingRule().getExcessCharging().getWindow());
    }

    Context context() {
        return new Context();
    }

    final class Context {
        final LazyValue<Scooter.State> scooterState = new LazyValue<>(scooter::state);
        final LazyValue<Capacity> charge = new LazyValue<>(scooter::charge);
        final LazyValue<Watt> recentExcess = new LazyValue<>(() -> electricity.excessProduction(P75, excessWindow));
        final LazyValue<Watt> currentExcess = new LazyValue<>(
                () -> min(electricity.excessProduction(), recentExcess.lazy()));
    }
}
