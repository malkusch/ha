package de.malkusch.ha.automation.model.shutters;

import static java.util.Objects.requireNonNull;

import de.malkusch.ha.automation.model.astronomy.Azimuth;

public record DirectSunLightRange(Azimuth start, Azimuth end) {

    public DirectSunLightRange(Azimuth start, Azimuth end) {
        this.start = requireNonNull(start);
        this.end = requireNonNull(end);
        if (start.isLessThan(start)) {
            throw new IllegalArgumentException(String.format("end %s must not be less than start %s", end, start));
        }
    }

    @Override
    public String toString() {
        return String.format("%s - %s", start, end);
    }

}
