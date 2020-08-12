package de.malkusch.ha.automation.infrastructure.trashday;

import java.util.Optional;

import de.malkusch.ha.automation.model.trashday.TrashCan;

interface TrashCanMapper {
    Optional<TrashCan> toTrashCan(String summary);
}
