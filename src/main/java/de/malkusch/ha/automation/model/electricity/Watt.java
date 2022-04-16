package de.malkusch.ha.automation.model.electricity;

import lombok.Value;

@Value
public final class Watt {

    private final double value;

    public Watt(double value) {
        if (value < 0) {
            throw new IllegalArgumentException("Watt must not be negative");
        }
        this.value = value;
    }

    public static Watt kilowatt(double kilowatt) {
        return new Watt(kilowatt * 1000);
    }

    public boolean isZero() {
        return value == 0;
    }

    public boolean isGreaterThan(Watt other) {
        return value > other.value;
    }

    public boolean isLessThan(Watt other) {
        return value < other.value;
    }

    public Watt plus(Watt buffer) {
        return new Watt(value + buffer.value);
    }

    public Watt minus(Watt buffer) {
        return new Watt(value - buffer.value);
    }

    public static Watt min(Watt a, Watt b) {
        return a.isLessThan(b) ? a : b;
    }

    @Override
    public String toString() {
        return String.format("%.2f W", value);
    }
}
