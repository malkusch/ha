package de.malkusch.ha.automation.infrastructure;

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
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.malkusch.ha.automation.infrastructure.prometheus.Prometheus;
import de.malkusch.ha.automation.model.heater.Heater;
import de.malkusch.ha.shared.model.ApiException;
import de.malkusch.km200.KM200;
import de.malkusch.km200.KM200Exception;
import de.malkusch.km200.KM200Exception.NotFound;

@Service
class KM200Heater implements Heater {

    private final KM200 km200;
    private final Prometheus prometheus;
    private final ObjectMapper mapper;

    public KM200Heater(KM200 km200, Prometheus prometheus, ObjectMapper mapper)
            throws InterruptedException, ApiException {

        this.km200 = km200;
        this.prometheus = prometheus;
        this.mapper = mapper;
    }

    @Override
    public boolean isHeating() throws ApiException, InterruptedException {
        var delta = prometheus.query("delta(heater_heatSources_workingTime_totalSystem[5m:])");
        return delta.compareTo(BigDecimal.ZERO) > 0;
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
