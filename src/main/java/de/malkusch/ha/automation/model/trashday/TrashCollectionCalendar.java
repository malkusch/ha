package de.malkusch.ha.automation.model.trashday;

import java.time.LocalDate;
import java.util.Collection;

public interface TrashCollectionCalendar {

    Collection<TrashCan> findTrashCollection(LocalDate tomorrow);

}
