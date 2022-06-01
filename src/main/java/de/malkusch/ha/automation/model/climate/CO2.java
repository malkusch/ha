package de.malkusch.ha.automation.model.climate;

public final record CO2(int ppm) {

    public CO2(int ppm) {
        if (ppm < 390) {
            throw new IllegalArgumentException("CO2 must not be less than 390 ppm");
        }
        if (ppm > 100000) {
            throw new IllegalArgumentException("CO2 must not be more than 100000 ppm");
        }
        this.ppm = ppm;
    }

    public boolean isGreaterThan(CO2 other) {
        return ppm > other.ppm;
    }

    public boolean isLessThan(CO2 other) {
        return ppm < other.ppm;
    }

    @Override
    public String toString() {
        return String.format("%s ppm", ppm);
    }
}
