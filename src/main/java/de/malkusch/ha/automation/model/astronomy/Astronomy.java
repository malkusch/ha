package de.malkusch.ha.automation.model.astronomy;

import java.time.LocalDate;
import java.util.List;

public interface Astronomy {

    List<AstronomicalEvent> calculateEvents(LocalDate date);

}
