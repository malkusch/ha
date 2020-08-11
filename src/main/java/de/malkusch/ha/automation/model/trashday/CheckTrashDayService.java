package de.malkusch.ha.automation.model.trashday;

import static de.malkusch.ha.shared.infrastructure.event.EventPublisher.publish;
import static java.time.LocalDate.now;

import java.util.Collection;

import org.springframework.stereotype.Service;

import de.malkusch.ha.shared.infrastructure.event.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public final class CheckTrashDayService {

    private final TrashCollectionCalendar calendar;

    public void checkTomorrow() {
        log.debug("Checking trash day");
        var tomorrow = now().plusDays(1);
        var trashCans = calendar.findTrashCollection(tomorrow);
        if (trashCans.isEmpty()) {
            return;
        }
        log.info("Noticed tomorrow's trash day for {}", trashCans);
        publish(new TomorrowsTrashDayNoticed(trashCans));
    }

    @RequiredArgsConstructor
    public static final class TomorrowsTrashDayNoticed implements Event {
        public final Collection<TrashCan> trashCans;
    }
}
