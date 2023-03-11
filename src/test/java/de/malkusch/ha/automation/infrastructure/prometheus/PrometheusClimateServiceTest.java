package de.malkusch.ha.automation.infrastructure.prometheus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.malkusch.ha.automation.model.Percent;
import de.malkusch.ha.automation.model.climate.CO2;
import de.malkusch.ha.automation.model.climate.Dust;
import de.malkusch.ha.automation.model.climate.Dust.PM2_5;
import de.malkusch.ha.automation.model.climate.Humidity;
import de.malkusch.ha.automation.model.room.RoomId;
import de.malkusch.ha.shared.infrastructure.http.HttpClient;
import de.malkusch.ha.shared.infrastructure.http.HttpResponse;

@ExtendWith(MockitoExtension.class)
public class PrometheusClimateServiceTest {

    @Mock
    private HttpClient http;

    private PrometheusClimateService climate;

    private static final RoomId ANY = new RoomId("any");
    private static final String ANY_SENSOR = "anysensor";
    private static final RoomId ANOTHER = new RoomId("another");
    private static final String ANOTHER_SENSOR = "anothersensor";

    @BeforeEach
    void setupPrometheus() {
        var prometheus = new PrometheusHttpClient(http, new ObjectMapper(), "http://example.org");
        climate = new PrometheusClimateService(prometheus, "aussen", Map.of(ANY, ANY_SENSOR, //
                ANOTHER, ANOTHER_SENSOR //
        ));
    }

    @ParameterizedTest
    @CsvSource({ "any, 0, 0, anysensor_humidity", //
            "any, , 0, anysensor_humidity", //
            "any, 1, 0.01, anysensor_humidity", //
            "any, 99, 0.99, anysensor_humidity", //
            "any, 100, 1, anysensor_humidity", //
            "another, 100, 1, anothersensor_humidity", //
    })
    void testHumidity(String roomString, String response, String expectedString, String expectedQuery)
            throws Exception {
        var room = room(roomString);
        mock(response);

        var result = climate.humidity(room);

        assertEquals(humidity(expectedString), result);
        verifyQuery(expectedQuery);
    }

    @ParameterizedTest
    @CsvSource({ "any, 0, 0, anysensor_pm25", //
            "any, , 0, anysensor_pm25", //
            "any, 1, 1, anysensor_pm25", //
            "any, 1.0, 1, anysensor_pm25", //
            "any, 1.1, 1.1, anysensor_pm25", //
            "any, 99, 99, anysensor_pm25", //
            "any, 100, 100, anysensor_pm25", //
            "another, 100, 100, anothersensor_pm25", //
    })
    void testDust(String roomString, String response, String expectedString, String expectedQuery) throws Exception {
        var room = room(roomString);
        mock(response);

        var result = climate.dust(room);

        assertEquals(dust(expectedString), result.get());
        verifyQuery(expectedQuery);
    }

    @ParameterizedTest
    @CsvSource({ "0, 0, aussen_pm25", //
            ", 0, aussen_pm25", //
            "1, 1, aussen_pm25", //
            "1.0, 1, aussen_pm25", //
            "1.1, 1.1, aussen_pm25", //
            "99, 99, aussen_pm25", //
            "100, 100, aussen_pm25", //
    })
    void testOutsideDust(String response, String expectedString, String expectedQuery) throws Exception {
        mock(response);

        var result = climate.outsideDust();

        assertEquals(dust(expectedString), result);
        verifyQuery(expectedQuery);
    }

    @ParameterizedTest
    @CsvSource({ "any, 391, 391, quantile_over_time(0.95, anysensor_co2[5m])", //
            "any, 400, 400, quantile_over_time(0.95, anysensor_co2[5m])", //
            "another, 400, 400, quantile_over_time(0.95, anothersensor_co2[5m])", //
    })
    void testCo2(String roomString, String response, String expectedString, String expectedQuery) throws Exception {
        var room = room(roomString);
        mock(response);

        var result = climate.co2(room);

        assertEquals(co2(expectedString), result.get());
        verifyQuery(expectedQuery);
    }

    private void verifyQuery(String query) throws IOException, InterruptedException {
        var path = ".*\\Q" + PrometheusHttpClient.encode(query) + "\\E.*";
        verify(http).get(matches(path));
    }

    private void mock(String value) throws IOException, InterruptedException {
        if (value == null) {
            mockEmpty();
        } else {
            mockValue(value);
        }
    }

    private void mockValue(String value) throws IOException, InterruptedException {
        when(http.get(any())).thenReturn(response(String.format("""
                {"status":"success","data":{"resultType":"vector","result":[{"metric":{"instance":"localhost:8090","job":"ha"},"value":[1678541585.468, "%s"]}]}}
                """, value)));
    }

    private void mockEmpty()  throws IOException, InterruptedException {
        when(http.get(any())).thenReturn(response("""
                {"status":"success","data":{"resultType":"vector","result":[]}}
                """));
    }

    private static HttpResponse response(String body) {
        return new HttpResponse(200, "http://example.org/", false, new ByteArrayInputStream(body.getBytes()));
    }

    private static RoomId room(String room) {
        return new RoomId(room);
    }

    private static Humidity humidity(String humidity) {
        return new Humidity(new Percent(Double.valueOf(humidity)));
    }

    private static Dust dust(String dust) {
        return new Dust(new PM2_5(Double.valueOf(dust)));
    }

    private static CO2 co2(String co2) {
        return new CO2(Integer.valueOf(co2));
    }
}
