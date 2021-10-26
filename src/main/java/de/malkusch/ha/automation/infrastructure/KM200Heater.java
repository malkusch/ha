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
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.malkusch.ha.automation.model.heater.Heater;
import de.malkusch.ha.automation.model.heater.Temperature;
import de.malkusch.km200.KM200;
import de.malkusch.km200.KM200Exception.NotFound;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
class KM200Heater implements Heater {

    private final KM200 km200;
    private final ObjectMapper mapper;

    public KM200Heater(KM200 km200, ObjectMapper mapper) throws IOException, InterruptedException {
        this.km200 = km200;
        this.mapper = mapper;

        cacheHotWaterSwitchProgram();
    }

    @Override
    public HeaterProgram currentHeaterProgram() throws IOException, InterruptedException {
        var active = km200.queryString("/heatingCircuits/hc1/activeSwitchProgram");
        var program = switchProgram("/heatingCircuits/hc1/switchPrograms/" + active);
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
    public void changeTemporaryHeaterTemperatur(Temperature temperature) throws IOException, InterruptedException {
        km200.update(TEMPORAY_ROOM_SET_POINT, temperature.getValue());
        var current = new Temperature(km200.queryBigDecimal(TEMPORAY_ROOM_SET_POINT));
        if (!current.equals(temperature)) {
            throw new IOException(
                    String.format("changeTemporaryHeaterTemperatur() to %s resulted in %s", temperature, current));
        }
    }

    private static final Temperature RESET_TEMPERATURE = new Temperature(-1);

    @Override
    public void resetTemporaryHeaterTemperatur() throws IOException, InterruptedException {
        changeTemporaryHeaterTemperatur(RESET_TEMPERATURE);
    }

    @Override
    public Temperature dayTemperature() throws IOException, InterruptedException {
        return new Temperature(km200.queryBigDecimal("/heatingCircuits/hc1/temperatureLevels/comfort2"));
    }

    private static final String HOT_WATER_OPERATION_MODE = "/dhwCircuits/dhw1/operationMode";

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
    public HotWaterMode ownProgramHotWaterMode() throws IOException, InterruptedException {
        return hotWaterMode(hotWaterSwitchProgram().setPointAt(now()));
    }

    private void cacheHotWaterSwitchProgram() throws IOException, InterruptedException {
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

    private SwitchProgram hotWaterSwitchProgram() throws IOException, InterruptedException {
        try {
            hotWaterSwitchProgram = switchProgram("/dhwCircuits/dhw1/switchPrograms/A");
            log.debug("Updated fallback switchProgram");

        } catch (NotFound e) {
            if (hotWaterSwitchProgram == null) {
                throw e;
            }
            log.debug("Returning fallback switchProgram");
        }
        return hotWaterSwitchProgram;
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

    LocalDateTime now() throws IOException, InterruptedException {
        return LocalDateTime.parse(km200.queryString("/gateway/DateTime"));
    }
}
