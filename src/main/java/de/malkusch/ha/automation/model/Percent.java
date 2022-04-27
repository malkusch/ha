package de.malkusch.ha.automation.model;

public record Percent(double value) {

    public static final Percent ZERO = new Percent(0);

    public Percent(double value) {
        if (value < 0 || value > 1) {
            throw new IllegalArgumentException("Percent must be between 0 and 1");
        }
        this.value = value;
    }

    public Percent(int percent) {
        this(percent / 100.0);
    }

    public boolean isGreaterThan(Percent other) {
        return this.value > other.value;
    }

    public boolean isLessThan(Percent other) {
        return this.value < other.value;
    }

    @Override
    public String toString() {
        return String.format("%.2f%%", value * 100);
    }
}
