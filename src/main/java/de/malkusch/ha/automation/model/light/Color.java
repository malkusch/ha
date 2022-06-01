package de.malkusch.ha.automation.model.light;

import static java.util.Objects.requireNonNull;

public record Color(Color.Red red, Color.Green green, Color.Blue blue) {

    public Color(int red, int green, int blue) {
        this(new Red(red), new Green(green), new Blue(blue));
    }

    public Color(Color.Red red, Color.Green green, Color.Blue blue) {
        this.red = requireNonNull(red);
        this.green = requireNonNull(green);
        this.blue = requireNonNull(blue);
    }

    public static record Red(int value) {

        public Red(int value) {
            assertValidColorValue(value);
            this.value = value;
        }
    }

    public static record Green(int value) {

        public Green(int value) {
            assertValidColorValue(value);
            this.value = value;
        }
    }

    public static record Blue(int value) {

        public Blue(int value) {
            assertValidColorValue(value);
            this.value = value;
        }
    }

    private static void assertValidColorValue(int value) {
        if (value < 0 || value > 255) {
            throw new IllegalArgumentException("Color must be between 0 and 255");
        }
    }
}