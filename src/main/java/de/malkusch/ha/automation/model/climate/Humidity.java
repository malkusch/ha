package de.malkusch.ha.automation.model.climate;

import static java.util.Objects.requireNonNull;

import de.malkusch.ha.automation.model.Percent;

public final record Humidity(Percent value) {

    public Humidity(Percent value) {
        this.value = requireNonNull(value);
    }

    public boolean isGreaterThan(Humidity other) {
        return value.isGreaterThan(other.value);
    }

    public boolean isLessThan(Humidity other) {
        return value.isLessThan(other.value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
