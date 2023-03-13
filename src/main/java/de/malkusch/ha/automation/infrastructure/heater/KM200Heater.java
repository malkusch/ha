package de.malkusch.ha.automation.infrastructure.heater;

import static de.malkusch.ha.automation.infrastructure.prometheus.Prometheus.AggregationQuery.Aggregation.DELTA;
import static java.math.BigDecimal.ZERO;
import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.TUESDAY;
import static java.time.DayOfWeek.WEDNESDAY;
import static java.time.LocalTime.MIDNIGHT;
import static java.util.Comparator.comparingInt;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.malkusch.ha.automation.infrastructure.prometheus.Prometheus;
import de.malkusch.ha.automation.infrastructure.prometheus.Prometheus.SimpleQuery;
import de.malkusch.ha.automation.model.Temperature;
import de.malkusch.ha.automation.model.heater.Heater;
import de.malkusch.ha.shared.model.ApiException;
import de.malkusch.km200.KM200;
import de.malkusch.km200.KM200Exception;
import de.malkusch.km200.KM200Exception.NotFound;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
class KM200Heater implements Heater {

    private final KM200 km200;
    private final Prometheus prometheus;
    private final ObjectMapper mapper;

    private static final Duration IS_HEATING_WINDOW = Duration.ofMinutes(5);
    private static final Prometheus.Query IS_HEATING = new SimpleQuery("heater_heatSources_workingTime_totalSystem") //
            .subquery(IS_HEATING_WINDOW) //
            .aggregate(DELTA);

    @Override
    public boolean isHeating() throws ApiException, InterruptedException {
        var result = prometheus.query(IS_HEATING);
        return result.compareTo(ZERO) > 0;
    }

    private static final String HEATER_TEMPERATURE = "/heatingCircuits/hc1/manualRoomSetpoint";

    @Override
    public Temperature heaterTemperature() throws ApiException, InterruptedException {
        return new Temperature(withApiException(() -> km200.queryBigDecimal(HEATER_TEMPERATURE)));
    }

    @Override
    public void changeHeaterTemperature(Temperature temperature) throws ApiException, InterruptedException {
        log.debug("Change temperature to {}", temperature);
        withApiException(() -> km200.update(HEATER_TEMPERATURE, temperature.getValue()));
        var current = heaterTemperature();
        if (!current.equals(temperature)) {
            throw new ApiException(
                    String.format("changeHeaterTemperature() to %s resulted in %s", temperature, current));
        }
    }

    SwitchProgram switchProgram(String path) throws IOException, InterruptedException {
        return mapper.readValue(km200.query(path), SwitchProgram.class);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static record SwitchProgram(List<SwitchPoint> switchPoints) {
        private static record SwitchPoint(String dayOfWeek, String setpoint, int time) {

            LocalTime localTime() {
                return MIDNIGHT.plusMinutes(time);
            }

            private static final Map<String, DayOfWeek> DAYS = Map.of("Mo", MONDAY, "Tu", TUESDAY, "We", WEDNESDAY,
                    "Th", THURSDAY, "Fr", FRIDAY, "Sa", SATURDAY, "Su", SUNDAY);

            DayOfWeek day() {
                return DAYS.get(dayOfWeek);
            }

            int index() {
                return index(day(), localTime());
            }

            private static int index(DayOfWeek day, LocalTime time) {
                return day.getValue() * 100000 + time.toSecondOfDay();
            }
        }

        String setPointAt(LocalDateTime date) {
            var dateIndexed = SwitchProgram.SwitchPoint.index(date.getDayOfWeek(), date.toLocalTime());
            var last = switchPoints.stream().max(comparingInt(SwitchProgram.SwitchPoint::index)).get();
            return switchPoints.stream() //
                    .filter(it -> it.index() <= dateIndexed) //
                    .min(comparingInt(it -> dateIndexed - it.index())) //
                    .orElse(last).setpoint();
        }
    }

    LocalDateTime now() throws ApiException, InterruptedException {
        return LocalDateTime.parse(withApiException(() -> km200.queryString("/gateway/DateTime")));
    }

    @FunctionalInterface
    private static interface Query<T> {
        T query() throws IOException, InterruptedException;
    }

    private <T> T withApiException(Query<T> query) throws ApiException, InterruptedException {
        try {
            return query.query();
        } catch (IOException e) {
            throw new ApiException(e);
        }
    }

    @FunctionalInterface
    private static interface Update {
        void update() throws IOException, InterruptedException;
    }

    private void withApiException(Update update) throws ApiException, InterruptedException {
        try {
            update.update();
        } catch (IOException e) {
            throw new ApiException(e);
        }
    }

    public boolean isHoliday() throws ApiException, InterruptedException {
        var now = now().toLocalDate();
        try {
            for (var i = 1; i <= 100; i++) {
                var path = "/system/holidayModes/hm" + i + "/startStop";
                var startstop = km200.queryString(path).split("/");
                var start = LocalDate.parse(startstop[0]);
                var stop = LocalDate.parse(startstop[1]);
                if (start.compareTo(now) <= 0 && stop.compareTo(now) >= 0) {
                    return true;
                }
            }
            throw new IllegalStateException("Iterated too many holiday modes");

        } catch (NotFound e) {
            return false;
        } catch (KM200Exception | IOException e) {
            throw new ApiException(e);
        }
    }
}
