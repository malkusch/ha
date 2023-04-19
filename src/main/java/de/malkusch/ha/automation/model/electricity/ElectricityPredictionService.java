package de.malkusch.ha.automation.model.electricity;

import static de.malkusch.ha.automation.model.electricity.Electricity.Aggregation.MAXIMUM;
import static java.util.Comparator.comparingDouble;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;

import de.malkusch.ha.automation.model.electricity.ElectricityPredictionService.SolarIrradianceForecast.Irradiance;
import de.malkusch.ha.shared.model.ApiException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ElectricityPredictionService {

    private final Electricity electricity;
    private final Wallbox wallbox;
    private final Watt minimumPeak;
    private final Duration peakWindow;
    private final SolarIrradianceForecast irradianaceForecast;
    private final Duration learningWindow;
    private volatile Irradiance threshold = Irradiance.MAX;

    public static interface SolarIrradianceForecast {

        Irradiance globalIrradiance(LocalDate date);

        public static record Irradiance(double megajoule) {

            public static final Irradiance MAX = new Irradiance(Double.MAX_VALUE);
            public static final Irradiance MIN = new Irradiance(0.01);

            public Irradiance {
                if (megajoule <= 0) {
                    throw new IllegalArgumentException("Must be greater than zero");
                }
            }

            public boolean isGreaterThan(Irradiance other) {
                return megajoule > other.megajoule;
            }

            public boolean isLessThan(Irradiance other) {
                return megajoule < other.megajoule;
            }

            @Override
            public String toString() {
                return String.format("%.2f MJ/m²", megajoule);
            }
        }

    }

    public ElectricityPredictionService(Electricity electricity, Watt minimumPeak, Duration peakWindow,
            SolarIrradianceForecast irradianceForecast, Duration learningWindow, Wallbox wallbox)
            throws ApiException, InterruptedException {

        this.electricity = electricity;
        this.minimumPeak = minimumPeak;
        this.peakWindow = peakWindow;
        this.irradianaceForecast = irradianceForecast;
        this.learningWindow = learningWindow;
        this.wallbox = wallbox;

        threshold = learnThreshold();
    }

    @Scheduled(cron = "${electricity.prediction.learn-cron}")
    void updateThreshold() throws ApiException, InterruptedException {
        threshold = learnThreshold();
    }

    private Irradiance learnThreshold() throws ApiException, InterruptedException {
        List<Irradiance> fullyCharged = new ArrayList<>();
        List<Irradiance> notFullyCharged = new ArrayList<>();
        var today = LocalDate.now();
        for (var day = 1; day < learningWindow.toDays(); day++) {
            var date = today.minusDays(day);

            var irradiance = irradianaceForecast.globalIrradiance(date);
            log.debug("Irradiance at {} is {}", date, irradiance);

            if (electricity.wasFullyCharged(date)) {
                log.debug("Adding irradiance {} from {} as candidate", irradiance, date);
                fullyCharged.add(irradiance);
            } else {
                if (wallbox.isLoadingWhileProducingElectricity(date)) {
                    log.debug("Ignoring {} as wallbox was used", date);
                    continue;
                }

                log.debug("Adding irradiance {} from {} as negative candidate", irradiance, date);
                notFullyCharged.add(irradiance);
            }
        }

        var maxNotFullyCharged = notFullyCharged.stream() //
                .max(comparingDouble(Irradiance::megajoule)) //
                .orElse(Irradiance.MIN);
        log.debug("Not fully charged threshold {}", maxNotFullyCharged);

        var threshold = fullyCharged.stream() //
                .filter(maxNotFullyCharged::isLessThan) //
                .min(comparingDouble(Irradiance::megajoule)) //
                .orElse(this.threshold);
        log.debug("Learned irradiance threshold for a full battery is {}", threshold);
        return threshold;
    }

    public boolean predictLoadedBattery() throws ApiException, InterruptedException {
        var peak = electricity.production(MAXIMUM, peakWindow);
        if (peak.isLessThan(minimumPeak)) {
            log.debug("Predict no loaded battery: Peak {} is less than {}", peak, minimumPeak);
            return false;
        }

        var irradiance = irradianaceForecast.globalIrradiance(LocalDate.now());
        log.debug("Irradiance is {}", irradiance);
        if (irradiance.isGreaterThan(threshold)) {
            log.debug("Predict loaded battery: Irradiance {} is greater than {}", irradiance, threshold);
            return true;
        }

        log.debug("Predict no loaded battery, as default");
        return false;
    }
}
