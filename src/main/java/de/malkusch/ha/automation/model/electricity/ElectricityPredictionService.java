package de.malkusch.ha.automation.model.electricity;

import static de.malkusch.ha.automation.model.electricity.Electricity.Aggregation.MAXIMUM;
import static de.malkusch.ha.automation.model.weather.Cloudiness.NO_CLOUDS;
import static java.util.Comparator.comparingDouble;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;

import de.malkusch.ha.automation.model.weather.Cloudiness;
import de.malkusch.ha.automation.model.weather.Weather;
import de.malkusch.ha.shared.model.ApiException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ElectricityPredictionService {

    private final Electricity electricity;
    private final Watt minimumPeak;
    private final Duration peakWindow;
    private final Weather weather;
    private volatile Cloudiness threshold = NO_CLOUDS;

    public ElectricityPredictionService(Electricity electricity, Watt minimumPeak, Duration peakWindow, Weather weather)
            throws ApiException, InterruptedException {

        this.electricity = electricity;
        this.minimumPeak = minimumPeak;
        this.peakWindow = peakWindow;
        this.weather = weather;

        threshold = learnCloudinessThreshold();
    }

    @Scheduled(cron = "${electricity.prediction.learn-cron}")
    void updateCloudinessThreshold() throws ApiException, InterruptedException {
        threshold = learnCloudinessThreshold();
    }

    private Cloudiness learnCloudinessThreshold() throws ApiException, InterruptedException {
        var window = 5;

        List<Cloudiness> candidates = new ArrayList<>();
        for (var day = 1; day < window; day++) {
            var date = LocalDate.now().minusDays(day);
            var cloudiness = weather.averageDaylightCloudiness(date);
            log.debug("Average cloudiness at {} is {}", date, cloudiness);

            if (electricity.wasFullyCharged(date)) {
                log.debug("Adding cloudiness {} from {} as candidate", cloudiness, date);
                candidates.add(cloudiness);
            } else {
                log.debug("Removing all candidates greater than", cloudiness);
                candidates.removeIf(it -> it.isGreaterThan(cloudiness));
            }
        }

        var max = candidates.stream().max(comparingDouble(Cloudiness::cloudiness)).orElse(NO_CLOUDS);
        log.debug("Learned cloudiness threshold for a full battery is {}", max);
        return max;
    }

    public boolean predictLoadedBattery() throws ApiException, InterruptedException {
        var peak = electricity.production(MAXIMUM, peakWindow);
        if (peak.isLessThan(minimumPeak)) {
            log.debug("Predict no loaded battery, because peak {} is too low", peak);
            return false;
        }

        var cloudiness = weather.averageDaylightCloudiness();
        log.debug("Cloudiness is {}", cloudiness);
        if (cloudiness.isLessThan(threshold)) {
            log.debug("Predict loaded battery, because of cloudiness {} is less than {}", cloudiness, threshold);
            return true;
        }

        log.debug("Predict no loaded battery, as default");
        return false;
    }
}
