package de.malkusch.ha.automation.model.geo;

public record Location(double latitude, double longitude) {

    public Location {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitute " + latitude + " is invalid");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("longitude " + longitude + " is invalid");
        }
    }

}
