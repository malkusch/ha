package de.malkusch.ha.automation.infrastructure.trashday;

import static java.util.stream.Collectors.toList;
import static net.fortuna.ical4j.model.Component.VEVENT;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.model.trashday.TrashCan;
import de.malkusch.ha.automation.model.trashday.TrashCollectionCalendar;
import de.malkusch.ha.shared.infrastructure.http.HttpClient;
import lombok.extern.slf4j.Slf4j;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.filter.Filter;
import net.fortuna.ical4j.filter.PeriodRule;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.component.CalendarComponent;

@Service
@Slf4j
final class ICallTrashCollectionCalendar implements TrashCollectionCalendar {

    private final HttpClient http;
    private final String url;
    private final TrashCanMapper mapper;
    private volatile Calendar calendar;

    ICallTrashCollectionCalendar(HttpClient http, @Value("${trashday.url}") String url, TrashCanMapper mapper)
            throws IOException, InterruptedException, ParserException {

        this.http = http;
        this.url = url;
        this.mapper = mapper;
        this.calendar = download();
    }

    @Override
    public Collection<TrashCan> findTrashCollection(LocalDate tomorrow) {
        update();

        var period = new Period<>(tomorrow, Duration.ofDays(1));
        var filter = new Filter<CalendarComponent>(new PeriodRule<>(period));
        var events = filter.filter(calendar.getComponents().get(VEVENT));

        return events.stream().map(mapper::toTrashCan).flatMap(Optional::stream).collect(toList());
    }

    private void update() {
        try {
            calendar = download();
        } catch (IOException | InterruptedException | ParserException e) {
            log.warn("Failed to update calendar {}", url, e);
        }
    }

    private Calendar download() throws IOException, InterruptedException, ParserException {
        log.debug("Downloading {}", url);
        try (var response = http.get(url)) {
            return new CalendarBuilder().build(response.body);
        }
    }
}
