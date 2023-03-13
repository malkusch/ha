package de.malkusch.ha.automation.model.climate;

import java.math.BigDecimal;

import de.malkusch.ha.automation.model.climate.Dust.PM2_5;

public final record Dust(PM2_5 pm2_5) {

    public final record PM2_5(double value) {
        
        public PM2_5(BigDecimal value) {
            this(value.doubleValue());
        }

        public boolean isGreaterThan(PM2_5 other) {
            return value > other.value;
        }

        public boolean isLessThan(PM2_5 other) {
            return value < other.value;
        }

        public PM2_5 plus(PM2_5 buffer) {
            return new PM2_5(value + buffer.value);
        }

        @Override
        public String toString() {
            return String.format("%.2f", value);
        }
    }

    @Override
    public String toString() {
        return String.format("pm2.5=%s", pm2_5);
    }
}
