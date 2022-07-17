package de.malkusch.ha.automation.model.astronomy;

import static java.util.Objects.requireNonNull;

public record Azimuth(double angle) {

    public Azimuth(double angle) {
        this.angle = requireNonNull(angle);
        if (angle < 0 || angle > 360) {
            throw new IllegalArgumentException("Angle must be between 0 and 360");
        }
    }

    public boolean isGreaterThan(Azimuth other) {
        return angle > other.angle;
    }

    public boolean isLessThan(Azimuth other) {
        return angle < other.angle;
    }

    @Override
    public String toString() {
        return String.format("%.2f°", angle);
    }
}
