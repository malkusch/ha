package de.malkusch.ha.automation.model.shutters;

import de.malkusch.ha.automation.model.astronomy.Azimuth;

import static java.util.Objects.requireNonNull;

public record DirectSunLightRange(Azimuth start, Azimuth end) {

    public static final DirectSunLightRange EMPTY = new DirectSunLightRange(new Azimuth(0), new Azimuth(0));

    public DirectSunLightRange(Azimuth start, Azimuth end) {
        this.start = requireNonNull(start);
        this.end = requireNonNull(end);
        if (start.isLessThan(start)) {
            throw new IllegalArgumentException(String.format("end %s must not be less than start %s", end, start));
        }
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }

    @Override
    public String toString() {
        return String.format("%s - %s", start, end);
    }

}
