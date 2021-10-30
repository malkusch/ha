package de.malkusch.ha.automation.infrastructure;

import static de.malkusch.ha.automation.model.heater.Heater.HotWaterMode.ECO;
import static de.malkusch.ha.automation.model.heater.Heater.HotWaterMode.HIGH;
import static de.malkusch.ha.automation.model.heater.Heater.HotWaterMode.LOW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.malkusch.ha.automation.infrastructure.prometheus.Prometheus;
import de.malkusch.ha.automation.model.heater.Heater;
import de.malkusch.km200.KM200;
import de.malkusch.km200.KM200Exception;

@ExtendWith(MockitoExtension.class)
public class KM200HeaterTest {

    @Mock
    private KM200 km200 = mock(KM200.class);

    private Heater heater;

    @BeforeEach
    public void setup() throws Exception {
        when(km200.query("/dhwCircuits/dhw1/switchPrograms/A")).thenReturn(
                """
                        {"id":"/dhwCircuits/dhw1/switchPrograms/A","type":"switchProgram","setpointProperty":{"id":"/dhwCircuits/dhw1/temperatureLevels","uri":"http://192.168.188.48/dhwCircuits/dhw1/temperatureLevels"},"maxNbOfSwitchPoints":42,"maxNbOfSwitchPointsPerDay":6,"switchPointTimeRaster":15,"writeable":1,
                        "switchPoints":[
                            {"dayOfWeek":"Mo","setpoint":"high","time":60},
                            {"dayOfWeek":"Mo","setpoint":"low","time":120},
                            {"dayOfWeek":"Mo","setpoint":"eco","time":180},

                            {"dayOfWeek":"Tu","setpoint":"high","time":240},
                            {"dayOfWeek":"Tu","setpoint":"low","time":300},
                            {"dayOfWeek":"Tu","setpoint":"eco","time":360},

                            {"dayOfWeek":"We","setpoint":"high","time":420},
                            {"dayOfWeek":"We","setpoint":"low","time":480},
                            {"dayOfWeek":"We","setpoint":"eco","time":540},

                            {"dayOfWeek":"Th","setpoint":"high","time":600},
                            {"dayOfWeek":"Th","setpoint":"low","time":660},
                            {"dayOfWeek":"Th","setpoint":"eco","time":720},

                            {"dayOfWeek":"Fr","setpoint":"high","time":780},
                            {"dayOfWeek":"Fr","setpoint":"low","time":840},
                            {"dayOfWeek":"Fr","setpoint":"eco","time":900},

                            {"dayOfWeek":"Sa","setpoint":"high","time":960},
                            {"dayOfWeek":"Sa","setpoint":"low","time":1020},
                            {"dayOfWeek":"Sa","setpoint":"eco","time":1080},

                            {"dayOfWeek":"Su","setpoint":"high","time":1140},
                            {"dayOfWeek":"Su","setpoint":"low","time":1200},
                            {"dayOfWeek":"Su","setpoint":"eco","time":1260}]}""");

        heater = new KM200Heater(km200, mock(Prometheus.class), new ObjectMapper());
    }

    @ParameterizedTest
    @ValueSource(strings = { "2021-10-11T00:59:59", "2021-10-11T03:00:00", "2021-10-12T03:59:59", "2021-10-12T06:00:00",
            "2021-10-13T06:59:59", "2021-10-13T09:00:00", "2021-10-14T09:59:59", "2021-10-14T12:00:00",
            "2021-10-15T12:59:59", "2021-10-15T15:00:00", "2021-10-16T15:59:59", "2021-10-16T18:00:00",
            "2021-10-17T18:59:59", "2021-10-17T21:00:00" })
    public void ownProgramHotWaterModeShouldReturnEco(String time) throws Exception {
        givenDateTime(time);

        var mode = heater.ownProgramHotWaterMode();

        assertEquals(ECO, mode);
    }

    @ParameterizedTest
    @ValueSource(strings = { "2021-10-11T02:00:00", "2021-10-11T02:59:59", "2021-10-12T05:00:00", "2021-10-12T05:59:59",
            "2021-10-13T08:00:00", "2021-10-13T08:59:59", "2021-10-14T11:00:00", "2021-10-14T11:59:59",
            "2021-10-15T14:00:00", "2021-10-15T14:59:59", "2021-10-16T17:00:00", "2021-10-16T17:59:59",
            "2021-10-17T20:00:00", "2021-10-17T20:59:59",

    })
    public void ownProgramHotWaterModeShouldReturnLow(String time) throws Exception {
        givenDateTime(time);

        var mode = heater.ownProgramHotWaterMode();

        assertEquals(LOW, mode);
    }

    @ParameterizedTest
    @ValueSource(strings = { "2021-10-11T01:00:00", "2021-10-11T01:59:59", "2021-10-12T04:00:00", "2021-10-12T04:59:59",
            "2021-10-13T07:00:00", "2021-10-13T07:59:59", "2021-10-14T10:00:00", "2021-10-14T10:59:59",
            "2021-10-15T13:00:00", "2021-10-15T13:59:59", "2021-10-16T16:00:00", "2021-10-16T16:59:59",
            "2021-10-17T19:00:00", "2021-10-17T19:59:59", })
    public void ownProgramHotWaterModeShouldReturnHigh(String time) throws Exception {
        givenDateTime(time);

        var mode = heater.ownProgramHotWaterMode();

        assertEquals(HIGH, mode);
    }

    @ParameterizedTest
    @ValueSource(strings = { "2021-10-31T00:00:00", "2021-11-02T23:59:59", })
    public void isHolidayShouldReturnTrueForHm1(String time) throws Exception {
        givenDateTime(time);
        when(km200.queryString("/system/holidayModes/hm1/startStop")).thenReturn("2021-10-31/2021-11-02");

        assertTrue(heater.isHoliday());
    }

    @ParameterizedTest
    @ValueSource(strings = { "2021-10-30T23:59:59", "2021-11-03T00:00:00", })
    public void isHolidayShouldReturnFalse(String time) throws Exception {
        givenDateTime(time);
        when(km200.queryString("/system/holidayModes/hm1/startStop")).thenReturn("2021-10-31/2021-11-02");
        when(km200.queryString("/system/holidayModes/hm2/startStop")).thenThrow(new KM200Exception.NotFound("Test"));

        assertFalse(heater.isHoliday());
    }

    @Test
    public void isHolidayShouldReturnTrueForHm2() throws Exception {
        givenDateTime("2021-11-05T00:00:00");
        when(km200.queryString("/system/holidayModes/hm1/startStop")).thenReturn("2021-10-31/2021-11-02");
        when(km200.queryString("/system/holidayModes/hm2/startStop")).thenReturn("2021-11-05/2021-11-05");

        assertTrue(heater.isHoliday());
    }

    private void givenDateTime(String time) throws Exception {
        when(km200.queryString("/gateway/DateTime")).thenReturn(time);
    }
}
