package de.malkusch.ha.automation.model.scooter;

public record Kilometers(double kilometers) {

    public Kilometers {
        if (kilometers < 0) {
            throw new IllegalArgumentException("Kilometers was negative");
        }
    }

    public boolean isZero() {
        return kilometers <= 0.001;
    }

    public boolean isGreaterThan(Kilometers other) {
        return kilometers > other.kilometers;
    }

    public Kilometers difference(Kilometers other) {
        return new Kilometers(kilometers - other.kilometers);
    }

    @Override
    public String toString() {
        return String.format("%.2f km", kilometers);
    }
}
