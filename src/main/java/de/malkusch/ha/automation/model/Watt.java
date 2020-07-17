package de.malkusch.ha.automation.model;

import lombok.Value;

@Value
public final class Watt {

    private final int value;

    public Watt(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("Watt must not be negative");
        }
        this.value = value;
    }

    public static Watt kilowatt(int kilowatt) {
        return new Watt(kilowatt * 1000);
    }

    public boolean isZero() {
        return value == 0;
    }

    public boolean isGreaterThan(Watt other) {
        return value > other.value;
    }

    public Watt plus(Watt buffer) {
        return new Watt(value + buffer.value);
    }

    @Override
    public String toString() {
        return String.format("%d Watt", value);
    }
}
