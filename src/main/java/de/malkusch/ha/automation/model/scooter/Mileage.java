package de.malkusch.ha.automation.model.scooter;

public record Mileage(Kilometers kilometers) {

    public static final Mileage MIN = new Mileage(1);

    public Mileage(double kilometers) {
        this(new Kilometers(kilometers));
    }

    public Mileage {
        if (kilometers.isZero()) {
            throw new IllegalArgumentException("Mileage must be greater than zero");
        }
    }

    public Kilometers difference(Mileage other) {
        return kilometers.difference(other.kilometers);
    }

    @Override
    public String toString() {
        return kilometers.toString();
    }
}
