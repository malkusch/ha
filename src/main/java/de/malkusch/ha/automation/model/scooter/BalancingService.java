package de.malkusch.ha.automation.model.scooter;

import static de.malkusch.ha.shared.infrastructure.DateUtil.formatDate;
import static de.malkusch.ha.shared.infrastructure.DateUtil.formatDuration;
import static de.malkusch.ha.shared.infrastructure.DateUtil.formatTime;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public final class BalancingService {

    private final Api api;

    public interface Api {
        Balancing lastBalancing() throws IOException;
    }

    public static record Balancing(Instant time, Mileage mileage) {

        public static final Balancing NEVER = new Balancing(Instant.EPOCH, Mileage.MIN);

        private static final Duration MAX_TIME_SKEW = Duration.ofMinutes(3);

        public Balancing(Instant time, Mileage mileage) {
            var now = Instant.now();
            if (time.isAfter(now)) {
                var difference = Duration.between(now, time).abs();
                if (difference.compareTo(MAX_TIME_SKEW) > 0) {
                    throw new IllegalArgumentException(String.format("time %s was %s to far in the future",
                            formatTime(time), formatDuration(difference)));
                }
                time = now;
            }

            this.time = time;
            this.mileage = mileage;
        }

        @Override
        public String toString() {
            return String.format("%s: %s", mileage, formatDate(time));
        }
    }

    public Balancing lastBalancing() throws IOException {
        return api.lastBalancing();
    }

}