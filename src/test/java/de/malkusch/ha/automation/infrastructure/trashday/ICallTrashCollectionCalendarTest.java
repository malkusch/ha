package de.malkusch.ha.automation.infrastructure.trashday;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import de.malkusch.ha.automation.model.trashday.TrashCan;
import de.malkusch.ha.automation.model.trashday.TrashCollectionCalendar;
import de.malkusch.ha.shared.infrastructure.http.HttpClient;
import de.malkusch.ha.shared.infrastructure.http.HttpResponse;

public class ICallTrashCollectionCalendarTest {

    private final HttpClient http = mock(HttpClient.class);
    private TrashCollectionCalendar calendar;

    @BeforeEach
    public void setupCalendar() throws Exception {
        var url = "ANY";
        when(http.get(url))
                .then(it -> new HttpResponse(200, url, false, getClass().getResourceAsStream("schedule.ics")));
        calendar = new ICallTrashCollectionCalendar(http, url, new DefaultMapper());
    }

    @ParameterizedTest
    @CsvSource({ "2017-05-26, RESIDUAL|ORGANIC", "2017-06-01, PAPER|PLASTIC","2017-06-09, RESIDUAL|ORGANIC", "2017-07-05, PAPER|PLASTIC",
            "2017-07-06, RESIDUAL|ORGANIC", "2017-07-20, RESIDUAL|ORGANIC" })
    public void shouldFindTrashCollection(String dateString, String expectedCans) {
        var date = LocalDate.parse(dateString);

        var trashCans = calendar.findTrashCollection(date);

        assertEquals(set(expectedCans), new HashSet<>(trashCans));
    }

    private static Set<TrashCan> set(String cans) {
        return stream(cans.split("\\|")).map(TrashCan::valueOf).collect(toSet());
    }
}
