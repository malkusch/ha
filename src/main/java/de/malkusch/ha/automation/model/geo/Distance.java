package de.malkusch.ha.automation.model.geo;

public record Distance(double meter) {

    public Distance {
        if (meter < 0) {
            throw new IllegalArgumentException("Negative distance " + meter);
        }
    }

    @Override
    public String toString() {
        if (meter >= 1000) {
            return String.format("%.2f km", meter / 1000);
        }
        return String.format("%.2f m", meter);
    }

    public boolean isGreaterThan(Distance distance) {
        return meter > distance.meter;
    }
}
