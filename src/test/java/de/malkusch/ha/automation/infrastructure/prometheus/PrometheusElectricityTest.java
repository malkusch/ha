package de.malkusch.ha.automation.infrastructure.prometheus;

import static de.malkusch.ha.automation.model.electricity.Electricity.Aggregation.MAXIMUM;
import static de.malkusch.ha.shared.infrastructure.DateUtil.toTimestamp;
import static java.util.Optional.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.malkusch.ha.automation.model.electricity.Capacity;
import de.malkusch.ha.automation.model.electricity.Electricity.Aggregation;
import de.malkusch.ha.automation.model.electricity.Watt;
import de.malkusch.ha.shared.infrastructure.http.HttpClient;
import de.malkusch.ha.shared.infrastructure.http.HttpResponse;

@ExtendWith(MockitoExtension.class)
public class PrometheusElectricityTest {

    @Mock
    private HttpClient http;

    private PrometheusElectricity electricity;

    @BeforeEach
    void setupPrometheus() {
        var prometheus = new PrometheusHttpClient(http, new ObjectMapper(), "http://example.org");
        var fullyCharged = new Capacity(1);
        electricity = new PrometheusElectricity(prometheus, fullyCharged);
    }

    @ParameterizedTest
    @CsvSource({ "MAXIMUM, PT1h, 2317, 2317", //
            "MAXIMUM, PT5m, 100, 100", //
            "MAXIMUM, PT5m, 0, 0", //
            "MAXIMUM, PT5m, ,0", //
            "MINIMUM, PT5m, ,0", //
            "P25, PT5m, ,0", //
            "P75, PT5m, ,0", //
            "MINIMUM, PT1h, 123, 123", //
            "P25, PT1h, 1234, 1234", //
            "P75, PT1h, 1235, 1235" })
    void testBatteryConsumption(String aggregationString, String durationString, String response, String expectedString)
            throws Exception {

        var aggregation = aggregation(aggregationString);
        var duration = duration(durationString);
        mock(response);

        var result = electricity.batteryConsumption(aggregation, duration);

        var expected = watt(expectedString);
        assertEquals(expected, result);
        verifyQuery(aggregation, "batterie_battery_consumption", duration);
    }

    @ParameterizedTest
    @CsvSource({ "0, 0", ", 0", "100, 1", "99, 0.99" })
    void testCapacity(String response, String expected) throws Exception {
        mock(response);

        var result = electricity.capacity();

        assertEquals(capacity(expected), result);
        verify(http).get(matches("batterie_charge"));
    }

    @ParameterizedTest
    @CsvSource({ //
            "MAXIMUM, PT1h, 123, 123", //
            "MAXIMUM, PT5m, 123, 123", //
            "MAXIMUM, PT1h, 0, 0", //
            "MAXIMUM, PT1h, , 0", //
            "MINIMUM, PT1h, 123, 123", //
            "P25, PT1h, 123, 123", //
            "P75, PT1h, 123, 123", //
    })
    void testConsumption(String aggregationString, String durationString, String response, String expected)
            throws Exception {

        var aggregation = aggregation(aggregationString);
        var duration = duration(durationString);
        mock(response);

        var result = electricity.consumption(aggregation, duration);

        assertEquals(watt(expected), result);
        verifyQuery(aggregation, "batterie_consumption", duration);
    }

    @ParameterizedTest
    @CsvSource({ "0, 0", ", 0", "100, 100", "99, 99" })
    void testExcess(String response, String expected) throws Exception {
        mock(response);

        var result = electricity.excess();

        assertEquals(watt(expected), result);
        verifyQuery("clamp_min(batterie_feed_in, 0)");
    }

    @ParameterizedTest
    @CsvSource({ //
            "MAXIMUM, PT1h, 123, 123", //
            "MAXIMUM, PT5m, 123, 123", //
            "MAXIMUM, PT1h, 0, 0", //
            "MAXIMUM, PT1h, , 0", //
            "MINIMUM, PT1h, 123, 123", //
            "P25, PT1h, 123, 123", //
            "P75, PT1h, 123, 123", //
    })
    void testExcess(String aggregationString, String durationString, String response, String expected)
            throws Exception {

        var aggregation = aggregation(aggregationString);
        var duration = duration(durationString);
        mock(response);

        var result = electricity.excess(aggregation, duration);

        assertEquals(watt(expected), result);
        verifyQuery(aggregation, "clamp_min(batterie_feed_in, 0)", duration);
    }

    @ParameterizedTest
    @CsvSource({ //
            "MAXIMUM, PT1h, 123, 123", //
            "MAXIMUM, PT5m, 123, 123", //
            "MAXIMUM, PT1h, 0, 0", //
            "MAXIMUM, PT1h, , 0", //
            "MINIMUM, PT1h, 123, 123", //
            "P25, PT1h, 123, 123", //
            "P75, PT1h, 123, 123", //
    })
    void testExcessProduction(String aggregationString, String durationString, String response, String expected)
            throws Exception {

        var aggregation = aggregation(aggregationString);
        var duration = duration(durationString);
        mock(response);

        var result = electricity.excessProduction(aggregation, duration);

        assertEquals(watt(expected), result);
        verifyQuery(aggregation, "clamp_min(batterie_production - batterie_consumption, 0)", duration);
    }

    @ParameterizedTest
    @CsvSource({ "0, 0", ", 0", "100, 100", "99, 99" })
    void testExcessProduction(String response, String expected) throws Exception {
        mock(response);

        var result = electricity.excessProduction();

        assertEquals(watt(expected), result);
        verifyQuery("clamp_min(batterie_production - batterie_consumption, 0)");
    }

    @ParameterizedTest
    @CsvSource({ //
            "MAXIMUM, PT1h, 123, 123", //
            "MAXIMUM, PT5m, 123, 123", //
            "MAXIMUM, PT1h, 0, 0", //
            "MAXIMUM, PT1h, , 0", //
            "MINIMUM, PT1h, 123, 123", //
            "P25, PT1h, 123, 123", //
            "P75, PT1h, 123, 123", //
    })
    void testProduction(String aggregationString, String durationString, String response, String expected)
            throws Exception {

        var aggregation = aggregation(aggregationString);
        var duration = duration(durationString);
        mock(response);

        var result = electricity.production(aggregation, duration);

        assertEquals(watt(expected), result);
        verifyQuery(aggregation, "batterie_production", duration);
    }

    @ParameterizedTest
    @CsvSource({ "2023-03-11, 0, false", //
            "2023-03-11,  , false", //
            "2023-03-11, 1, false", //
            "2023-03-11, 99, false", //
            "2023-03-11, 99.9, false", //
            "2023-03-11, 100, true", //
    })
    void testWasFullyCharged(String dateString, String response, boolean expected) throws Exception {
        var date = LocalDate.parse(dateString);
        mock(response);

        var result = electricity.wasFullyCharged(date);

        assertEquals(expected, result);
        verifyQuery(MAXIMUM, "batterie_charge", Duration.ofDays(1), date.plusDays(1));
    }

    @ParameterizedTest
    @CsvSource({ //
            "2023-03-11, 4000, PT30m, 1, true", //
            "2023-03-11, 4000, PT30m, 2, true", //
            "2023-03-11, 4000, PT30m, 0, false", //
    })
    void testIsConsumptionDuringProductionGreaterThan(String dateString, String thresholdString, String durationString,
            String response, boolean expected) throws Exception {

        var date = LocalDate.parse(dateString);
        var threshold = watt(thresholdString);
        var duration = duration(durationString);
        mock(response);

        var result = electricity.isConsumptionDuringProductionGreaterThan(date, threshold, duration);

        assertEquals(expected, result);
        verifyQuery(
                "(batterie_consumption > " + thresholdString + ".00 and batterie_production > 0)"
                        + expectedDuration(Duration.ofDays(1), duration), //
                date.plusDays(1));
    }

    private void verifyQuery(String query, LocalDate date) throws IOException, InterruptedException {
        verifyQuery(new QueryMatcher(empty(), query, empty(), Optional.of(date)));
    }

    private void verifyQuery(String query) throws IOException, InterruptedException {
        verifyQuery(new QueryMatcher(empty(), query, empty(), empty()));
    }

    private void verifyQuery(Aggregation aggregation, String name, Duration duration)
            throws IOException, InterruptedException {

        verifyQuery(new QueryMatcher(Optional.of(aggregation), name, Optional.of(duration), empty()));
    }

    private void verifyQuery(Aggregation aggregation, String name, Duration duration, LocalDate date)
            throws IOException, InterruptedException {

        verifyQuery(new QueryMatcher(Optional.of(aggregation), name, Optional.of(duration), Optional.ofNullable(date)));
    }

    private void verifyQuery(QueryMatcher matcher) throws IOException, InterruptedException {
        verify(http).get(matches(matcher.regexp()));
    }

    private static record QueryMatcher(Optional<Aggregation> aggregation, String query, Optional<Duration> duration,
            Optional<LocalDate> date) {

        public String regexp() {
            return aggregation.map(it -> ".*\\Q" + expectedAggregation(it) + "\\E.*").orElse("")//
                    + ".*\\Q" + PrometheusHttpClient.encode(query) + "\\E"//
                    + duration.map(it -> ".*\\Q" + PrometheusHttpClient.encode(expectedDuration(it)) + "\\E").orElse("") //
                    + date.map(it -> ".*&time=" + toTimestamp(it)).orElse("") //
                    + ".*";
        }
    }

    private static String expectedAggregation(Aggregation aggregation) {
        return PrometheusHttpClient.encode(switch (aggregation) {
        case MINIMUM -> "min_over_time";
        case P25 -> "quantile_over_time(0.25,";
        case MAXIMUM -> "max_over_time";
        case P75 -> "quantile_over_time(0.75,";
        });
    }

    private static String expectedDuration(Duration duration) {
        var seconds = duration.toSeconds();
        var prometheusDuration = String.format("[%ss:]", seconds);
        return prometheusDuration;
    }

    private static String expectedDuration(Duration duration, Duration aggregation) {
        var seconds = duration.toSeconds();
        var prometheusDuration = String.format("[%ss:%ss]", seconds, aggregation.toSeconds());
        return prometheusDuration;
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

    private static Duration duration(String duration) {
        return Duration.parse(duration);
    }

    private static Aggregation aggregation(String aggregation) {
        return Aggregation.valueOf(aggregation);
    }

    private static Watt watt(String watt) {
        return new Watt(Double.valueOf(watt));
    }

    private static Capacity capacity(String capacity) {
        return new Capacity(Double.valueOf(capacity));
    }
}
