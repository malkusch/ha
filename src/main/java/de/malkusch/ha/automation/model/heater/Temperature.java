package de.malkusch.ha.automation.model.heater;

import java.math.BigDecimal;

import lombok.Value;

@Value
public final class Temperature {

    private final BigDecimal value;

    public Temperature(double value) {
        this(BigDecimal.valueOf(value));
    }

    public Temperature(BigDecimal value) {
        this.value = value;
    }

    public Temperature minus(Temperature temperature) {
        return new Temperature(value.subtract(temperature.value));
    }

    public Temperature plus(Temperature temperature) {
        return new Temperature(value.add(temperature.value));
    }

    public Temperature multiply(int factor) {
        return new Temperature(value.multiply(BigDecimal.valueOf(factor)));
    }

    @Override
    public String toString() {
        return String.format("%.2f ' °C'", value.doubleValue());
    }
}
