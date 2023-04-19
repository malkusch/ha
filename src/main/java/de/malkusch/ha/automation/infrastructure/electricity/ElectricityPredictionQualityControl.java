package de.malkusch.ha.automation.infrastructure.electricity;

import static de.malkusch.ha.shared.infrastructure.DateUtil.formatTime;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.model.electricity.Electricity;
import de.malkusch.ha.automation.model.electricity.ElectricityPredictionService;
import de.malkusch.ha.automation.model.electricity.Wallbox;
import de.malkusch.ha.shared.model.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ElectricityPredictionQualityControl {

    private final ElectricityPredictionService predictionService;
    private final Electricity electricity;
    private final Wallbox wallbox;

    private static record Prediction(Instant timestamp, boolean predictLoadedBattery) {

        LocalDate date() {
            return LocalDate.ofInstant(timestamp, ZoneId.systemDefault());
        }

        LocalTime time() {
            return LocalTime.ofInstant(timestamp, ZoneId.systemDefault());
        }

        @Override
        public String toString() {
            return String.format("%s(loaded=%s, %s)", getClass().getSimpleName(), predictLoadedBattery,
                    formatTime(timestamp));
        }
    }

    private final List<Prediction> predictions = new CopyOnWriteArrayList<>();

    @Scheduled(cron = "${electricity.prediction.qa.predict-cron}")
    void registerPrediction() throws ApiException, InterruptedException {
        var prediction = new Prediction(Instant.now(), predictionService.predictLoadedBattery());
        log.debug("Registering prediction {}", prediction);
        predictions.add(prediction);
    }

    private List<LocalTime> predictions(boolean predictLoaded, LocalDate date) {
        return predictions.stream() //
                .filter(it -> it.date().equals(date)) //
                .filter(it -> it.predictLoadedBattery == predictLoaded) //
                .map(Prediction::time) //
                .map(it -> it.truncatedTo(ChronoUnit.MINUTES)) //
                .toList();
    }

    @Scheduled(cron = "${electricity.prediction.qa.evaluate-cron}")
    void evaluate() throws ApiException, InterruptedException {
        var yesterday = LocalDate.now().minusDays(1);
        var predictedLoaded = predictions(true, yesterday);
        var predictedNotLoaded = predictions(false, yesterday);
        var actual = electricity.wasFullyCharged(yesterday);
        var isWallboxLoading = wallbox.isLoadingWhileProducingElectricity(yesterday);

        log.info("predicted loaded at {}", predictedLoaded);
        log.info("predicted not loaded at {}", predictedNotLoaded);
        log.info("Battery was fully charged: {}", actual);
        log.info("Wallbox was loading: {}", isWallboxLoading);

        if (actual) {
            if (!predictedLoaded.isEmpty()) {
                log.info("Predicted correctly fully charged battery: {}", predictedLoaded);
            } else {
                log.info("Failed to predict fully charged battery");
            }

        } else {
            if (predictedLoaded.isEmpty()) {
                log.info("Predicted correctly not fully charged battery");
            } else {
                if (isWallboxLoading) {
                    log.info("Ignore wrongly predicted charged battery, because wallbox was loading");
                } else {
                    log.info("Predicted wrongly fully charged battery: {}", predictedLoaded);
                }
            }
        }

        predictions.removeIf(it -> !it.date().isAfter(yesterday));
        log.debug("Reduced registered predictions to {}", predictions.size());
    }

}
