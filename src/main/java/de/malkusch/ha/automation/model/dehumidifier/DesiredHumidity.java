package de.malkusch.ha.automation.model.dehumidifier;

import static java.util.Objects.requireNonNull;

import de.malkusch.ha.automation.model.climate.Humidity;

public record DesiredHumidity(Humidity minimum, Humidity maximum) {

    public DesiredHumidity(Humidity minimum, Humidity maximum) {
        this.minimum = requireNonNull(minimum);
        this.maximum = requireNonNull(maximum);
        if (maximum.isLessThan(minimum)) {
            throw new IllegalArgumentException(
                    String.format("maximum %s must not be less than minimum %s", maximum, minimum));
        }
    }
    

    @Override
    public String toString() {
        return String.format("%s - %s", minimum, maximum);
    }
}
