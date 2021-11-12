package de.malkusch.ha.automation.model.weather;

public record WindSpeed(double kmh) {

    public static WindSpeed fromMps(double mps) {
        return new WindSpeed(mps / 1000 * 60 * 60);
    }

    public double mps() {
        return kmh * 1000 / 60 / 60;
    }

    public boolean isGreaterThan(WindSpeed other) {
        return kmh > other.kmh;
    }

    public boolean isLessThan(WindSpeed other) {
        return kmh < other.kmh;
    }

    @Override
    public String toString() {
        return String.format("%.2f km/h", kmh);
    }
}
