package de.malkusch.ha.automation.infrastructure;

import static de.malkusch.ha.automation.model.heater.Heater.HotWaterMode.ECO;
import static de.malkusch.ha.automation.model.heater.Heater.HotWaterMode.HIGH;
import static de.malkusch.ha.automation.model.heater.Heater.HotWaterMode.LOW;
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
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.malkusch.ha.automation.model.heater.Heater;
import de.malkusch.km200.KM200;
import de.malkusch.km200.KM200Exception.NotFound;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
class KM200Heater implements Heater {

    private final KM200 km200;
    private final ObjectMapper mapper;

    private static final String HOT_WATER_OPERATION_MODE = "/dhwCircuits/dhw1/operationMode";

    public KM200Heater(KM200 km200, ObjectMapper mapper) throws IOException, InterruptedException {
        this.km200 = km200;
        this.mapper = mapper;

        cacheSwitchProgram();
    }

    @Override
    public void switchHotWaterMode(HotWaterMode mode) throws IOException, InterruptedException {
        km200.update(HOT_WATER_OPERATION_MODE, km200Mode(mode));
        SECONDS.sleep(1);
        var current = currentHotWaterMode();
        if (current != mode) {
            throw new IOException(String.format("Switching to %s resulted in %s", mode, current));
        }
    }

    @Override
    public HotWaterMode currentHotWaterMode() throws IOException, InterruptedException {
        return hotWaterMode(km200.queryString(HOT_WATER_OPERATION_MODE));
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
        default:
            throw new IllegalStateException("Invalid mode " + mode);
        }
    }

    @Override
    public HotWaterMode ownProgramHotWaterMode() throws IOException, InterruptedException {
        var now = now();
        var nowIndexed = SwitchProgram.SwitchPoint.index(now.getDayOfWeek(), now.toLocalTime());

        var program = switchProgram().switchPoints();
        var last = program.stream().max(comparingInt(SwitchProgram.SwitchPoint::index)).get();
        return program.stream() //
                .filter(it -> it.index() <= nowIndexed) //
                .min(comparingInt(it -> nowIndexed - it.index())) //
                .orElse(last).mode();
    }

    private void cacheSwitchProgram() throws IOException, InterruptedException {
        try {
            switchProgram();
        } catch (NotFound e) {
            var mode = currentHotWaterMode();
            if (mode == OWNPROGRAM) {
                throw e;
            }
            log.debug("Temporarily switching from {} to OWNPROGRAM to read fallback switch program", mode);
            try {
                switchHotWaterMode(OWNPROGRAM);
                SECONDS.sleep(1);
                switchProgram();

            } finally {
                log.debug("Switching back to {}", mode);
                switchHotWaterMode(mode);
            }
        }
    }

    private volatile SwitchProgram switchProgram;

    private SwitchProgram switchProgram() throws IOException, InterruptedException {
        try {
            switchProgram = mapper.readValue(km200.query("/dhwCircuits/dhw1/switchPrograms/A"), SwitchProgram.class);
            log.debug("Updated fallback switchProgram");

        } catch (NotFound e) {
            if (switchProgram == null) {
                throw e;
            }
            log.debug("Returning fallback switchProgram");
        }
        return switchProgram;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static record SwitchProgram(List<SwitchPoint> switchPoints) {
        private static record SwitchPoint(String dayOfWeek, String setpoint, int time) {

            HotWaterMode mode() {
                return hotWaterMode(setpoint);
            }

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
    }

    LocalDateTime now() throws IOException, InterruptedException {
        return LocalDateTime.parse(km200.queryString("/gateway/DateTime"));
    }
}
