package de.malkusch.ha.automation.infrastructure.trashday;

import static de.malkusch.ha.automation.model.trashday.TrashCan.ORGANIC;
import static de.malkusch.ha.automation.model.trashday.TrashCan.PAPER;
import static de.malkusch.ha.automation.model.trashday.TrashCan.PLASTIC;
import static de.malkusch.ha.automation.model.trashday.TrashCan.RESIDUAL;

import java.util.Optional;

import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.model.trashday.TrashCan;

@Service
final class DefaultMapper implements TrashCanMapper {

    @Override
    public Optional<TrashCan> toTrashCan(String summary) {

        if (summary.contains("Altpapier")) {
            return Optional.of(PAPER);

        } else if (summary.contains("Gelber Sack")) {
            return Optional.of(PLASTIC);

        } else if (summary.contains("Biomüll")) {
            return Optional.of(ORGANIC);

        } else if (summary.contains("Restmüll")) {
            return Optional.of(RESIDUAL);
        } else {
            return Optional.empty();
        }
    }
}
