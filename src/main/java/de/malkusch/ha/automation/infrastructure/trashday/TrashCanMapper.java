package de.malkusch.ha.automation.infrastructure.trashday;

import java.util.Optional;

import de.malkusch.ha.automation.model.trashday.TrashCan;
import net.fortuna.ical4j.model.Component;

public interface TrashCanMapper {
    Optional<TrashCan> toTrashCan(Component event);
}
