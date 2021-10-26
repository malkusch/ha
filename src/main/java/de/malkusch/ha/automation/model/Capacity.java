package de.malkusch.ha.automation.model;

import lombok.Value;

@Value
public final class Capacity {

    private final double value;

    public Capacity(double value) {
        if (value < 0 || value > 1) {
            throw new IllegalArgumentException("Capacity must be between 0 and 1, was " + value);
        }
        this.value = value;
    }

    public boolean isLessThan(Capacity other) {
        return value < other.value;
    }
    
    @Override
    public String toString() {
        return String.format("%.2f%%", value * 100);
    }
}
