package de.malkusch.ha.automation.model.climate;

import java.util.Optional;

import de.malkusch.ha.automation.model.room.RoomId;
import de.malkusch.ha.shared.model.ApiException;

public interface ClimateService {

    Humidity humidity(RoomId room) throws ApiException, InterruptedException;

    Optional<Dust> dust(RoomId room) throws ApiException, InterruptedException;

    Dust outsideDust() throws ApiException, InterruptedException;

    Optional<CO2> co2(RoomId room) throws ApiException, InterruptedException;

}
