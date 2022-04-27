package de.malkusch.ha.automation.model.weather;

import static java.util.Objects.requireNonNull;

import de.malkusch.ha.automation.model.Percent;

public record Cloudiness(Percent value) {

    public static final Cloudiness NO_CLOUDS = new Cloudiness(Percent.ZERO);

    public Cloudiness(Percent value) {
        this.value = requireNonNull(value);
    }

    public Cloudiness(int percent) {
        this(new Percent(percent));
    }

    public Cloudiness(double percent) {
        this(new Percent(percent));
    }

    public boolean isGreaterThan(Cloudiness other) {
        return value.isGreaterThan(other.value);
    }

    public boolean isLessThan(Cloudiness other) {
        return value.isLessThan(other.value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
