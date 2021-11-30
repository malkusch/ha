package de.malkusch.ha.automation.infrastructure;

import static de.malkusch.ha.automation.model.heater.Heater.HotWaterMode.ECO;
import static de.malkusch.ha.automation.model.heater.Heater.HotWaterMode.HIGH;
import static de.malkusch.ha.automation.model.heater.Heater.HotWaterMode.LOW;
import static de.malkusch.ha.automation.model.heater.Heater.HotWaterMode.OFF;
import static de.malkusch.ha.automation.model.heater.Heater.HotWaterMode.OWNPROGRAM;
import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.TUESDAY;
import static java.time.DayOfWeek.WEDNESDAY;
import static java.time.LocalTime.MIDNIGHT;
import static java.util.Comparator.comparingInt;
import static java.util.concurrent.TimeUnit.SECONDS;

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
import de.malkusch.ha.automation.model.Temperature;
import de.malkusch.ha.automation.model.heater.Heater;
import de.malkusch.ha.shared.model.ApiException;
import de.malkusch.km200.KM200;
import de.malkusch.km200.KM200Exception;
import de.malkusch.km200.KM200Exception.NotFound;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
class KM200Heater implements Heater {

    private final KM200 km200;
    private final Prometheus prometheus;
    private final ObjectMapper mapper;

    public KM200Heater(KM200 km200, Prometheus prometheus, ObjectMapper mapper)
            throws InterruptedException, ApiException {

        this.km200 = km200;
        this.prometheus = prometheus;
        this.mapper = mapper;

        cacheHotWaterSwitchProgram();
    }

    @Override
    public boolean isHeating() throws ApiException, InterruptedException {
        var delta = prometheus.query("delta(heater_heatSources_workingTime_totalSystem[5m:])");
        return delta.compareTo(BigDecimal.ZERO) > 0;
    }

    @Override
    public HeaterProgram currentHeaterProgram() throws ApiException, InterruptedException {
        var active = withApiException(() -> km200.queryString("/heatingCircuits/hc1/activeSwitchProgram"));
        var program = withApiException(() -> switchProgram("/heatingCircuits/hc1/switchPrograms/" + active));
        var setPoint = program.setPointAt(now());
        switch (setPoint) {
        case "eco":
            return HeaterProgram.NIGHT;
        case "comfort2":
            return HeaterProgram.DAY;
        default:
            throw new IllegalStateException("Invalid Heater program " + setPoint + " for active program " + active);
        }
    }

    private static final String TEMPORAY_ROOM_SET_POINT = "/heatingCircuits/hc1/temporaryRoomSetpoint";

    @Override
    public void changeTemporaryHeaterTemperatur(Temperature temperature) throws ApiException, InterruptedException {
        withApiException(() -> km200.update(TEMPORAY_ROOM_SET_POINT, temperature.getValue()));
        var current = new Temperature(withApiException(() -> km200.queryBigDecimal(TEMPORAY_ROOM_SET_POINT)));
        if (!current.equals(temperature)) {
            throw new ApiException(
                    String.format("changeTemporaryHeaterTemperatur() to %s resulted in %s", temperature, current));
        }
    }

    private static final Temperature RESET_TEMPERATURE = new Temperature(-1);

    @Override
    public void resetTemporaryHeaterTemperatur() throws ApiException, InterruptedException {
        changeTemporaryHeaterTemperatur(RESET_TEMPERATURE);
    }

    @Override
    public Temperature dayTemperature() throws ApiException, InterruptedException {
        return new Temperature(
                withApiException(() -> km200.queryBigDecimal("/heatingCircuits/hc1/temperatureLevels/comfort2")));
    }

    private static final String HOT_WATER_HIGH_TEMPERATURE = "/dhwCircuits/dhw1/temperatureLevels/high";

    @Override
    public Temperature hotwaterHighTemperature() throws ApiException, InterruptedException {
        return new Temperature(withApiException(() -> km200.queryBigDecimal(HOT_WATER_HIGH_TEMPERATURE)));
    }

    @Override
    public void changeHotwaterHighTemperature(Temperature temperature) throws ApiException, InterruptedException {
        withApiException(() -> km200.update(HOT_WATER_HIGH_TEMPERATURE, temperature.getValue()));
        var changed = hotwaterHighTemperature();
        if (!changed.equals(temperature)) {
            throw new ApiException(
                    String.format("Failed setting hot water HIGH temperature to %s, was %s", temperature, changed));
        }
    }

    private static final String HOT_WATER_OPERATION_MODE = "/dhwCircuits/dhw1/operationMode";

    @Override
    public void switchHotWaterMode(HotWaterMode mode) throws ApiException, InterruptedException {
        withApiException(() -> km200.update(HOT_WATER_OPERATION_MODE, km200Mode(mode)));
        SECONDS.sleep(2);
        var current = currentHotWaterMode();
        if (current != mode) {
            throw new ApiException(String.format("Switching to %s resulted in %s", mode, current));
        }
    }

    @Override
    public HotWaterMode currentHotWaterMode() throws ApiException, InterruptedException {
        return hotWaterMode(withApiException(() -> km200.queryString(HOT_WATER_OPERATION_MODE)));
    }

    private static HotWaterMode hotWaterMode(String mode) {
        switch (mode) {
        case "eco":
            return ECO;
        case "low":
            return LOW;
        case "high":
            return HIGH;
        case "ownprogram":
            return OWNPROGRAM;
        case "Off":
            return OFF;
        default:
            throw new IllegalStateException("Invalid mode " + mode);
        }
    }

    private static String km200Mode(HotWaterMode mode) {
        switch (mode) {
        case ECO:
            return "eco";
        case LOW:
            return "low";
        case HIGH:
            return "high";
        case OWNPROGRAM:
            return "ownprogram";
        case OFF:
            return "Off";
        default:
            throw new IllegalStateException("Invalid mode " + mode);
        }
    }

    @Override
    public HotWaterMode ownProgramHotWaterMode() throws ApiException, InterruptedException {
        var now = now();
        return hotWaterMode(hotWaterSwitchProgram().setPointAt(now));
    }

    private void cacheHotWaterSwitchProgram() throws ApiException, InterruptedException {
        try {
            hotWaterSwitchProgram();
        } catch (NotFound e) {
            var mode = currentHotWaterMode();
            if (mode == OWNPROGRAM) {
                throw e;
            }
            log.debug("Temporarily switching from {} to OWNPROGRAM to read fallback switch program", mode);
            try {
                switchHotWaterMode(OWNPROGRAM);
                SECONDS.sleep(1);
                hotWaterSwitchProgram();

            } finally {
                log.debug("Switching back to {}", mode);
                switchHotWaterMode(mode);
            }
        }
    }

    private volatile SwitchProgram hotWaterSwitchProgram;

    private SwitchProgram hotWaterSwitchProgram() throws ApiException, InterruptedException {
        return withApiException(() -> {
            try {
                hotWaterSwitchProgram = switchProgram("/dhwCircuits/dhw1/switchPrograms/A");
                log.debug("Updated fallback switchProgram");

            } catch (NotFound e) {
                if (hotWaterSwitchProgram == null) {
                    throw e;
                }
                log.debug("Returning fallbackwithApiException switchProgram");
            }
            return hotWaterSwitchProgram;
        });
    }

    private SwitchProgram switchProgram(String path) throws IOException, InterruptedException {
        return mapper.readValue(km200.query(path), SwitchProgram.class);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static record SwitchProgram(List<SwitchPoint> switchPoints) {
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

        private String setPointAt(LocalDateTime date) {
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

    @Override
    public boolean isWinter() {
        // TODO check in summer /heatingCircuits/hc1/currentSuWiMode, was in
        // winter "forced"
        var month = LocalDate.now().getMonthValue();
        return month >= 10 || month <= 3;
    }

    @Override
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
