package de.malkusch.ha.automation.infrastructure.trashday;

import static de.malkusch.ha.automation.model.trashday.TrashCan.ORGANIC;
import static de.malkusch.ha.automation.model.trashday.TrashCan.RESIDUAL;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    @Test
    public void shouldFindTrashCollection() {
        var date = LocalDate.parse("2017-05-26");

        var trashCans = calendar.findTrashCollection(date);

        assertEquals(set(RESIDUAL, ORGANIC), new HashSet<>(trashCans));
    }

    private static Set<TrashCan> set(TrashCan... cans) {
        return new HashSet<>(asList(cans));
    }
}
