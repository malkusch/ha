package de.malkusch.ha.automation.model.weather;

public record Cloudiness(double cloudiness) {

    public Cloudiness(double cloudiness) {
        if (cloudiness < 0 || cloudiness > 1) {
            throw new IllegalArgumentException("Cloudiness must be between 0 and 1");
        }
        this.cloudiness = cloudiness;
    }
    
    public Cloudiness(int percent) {
        this(percent / 100.0);
    }

    public boolean isGreaterThan(Cloudiness cloudiness) {
        return this.cloudiness > cloudiness.cloudiness;
    }

    @Override
    public String toString() {
        return String.format("%.2f%%", cloudiness * 100);
    }
}
