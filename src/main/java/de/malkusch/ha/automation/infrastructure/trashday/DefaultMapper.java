package de.malkusch.ha.automation.infrastructure.trashday;

import static de.malkusch.ha.automation.model.trashday.TrashCan.ORGANIC;
import static de.malkusch.ha.automation.model.trashday.TrashCan.PAPER;
import static de.malkusch.ha.automation.model.trashday.TrashCan.PLASTIC;
import static de.malkusch.ha.automation.model.trashday.TrashCan.RESIDUAL;
import static net.fortuna.ical4j.model.Property.SUMMARY;

import java.util.Optional;

import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.model.trashday.TrashCan;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;

@Service
final class DefaultMapper implements TrashCanMapper {

    @Override
    public Optional<TrashCan> toTrashCan(Component event) {
        var summary = event.getProperties().getFirst(SUMMARY);
        return summary.map(DefaultMapper::map);
    }

    private static TrashCan map(Property summaryProperty) {
        var summary = summaryProperty.getValue();
        if (summary == null) {
            return null;
        }
        if (summary.contains("Altpapier")) {
            return PAPER;

        } else if (summary.contains("Gelber Sack")) {
            return PLASTIC;

        } else if (summary.contains("Biomüll")) {
            return ORGANIC;

        } else if (summary.contains("Restmüll")) {
            return RESIDUAL;
        } else {
            return null;
        }
    }
}
