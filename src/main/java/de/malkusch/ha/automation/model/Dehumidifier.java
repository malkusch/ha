package de.malkusch.ha.automation.model;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@RequiredArgsConstructor
@EqualsAndHashCode(of = "id")
public final class Dehumidifier {

    @Value
    public static final class DehumidifierId {
        private final String id;
    }

    public static interface DehumidifierRepository {
        Dehumidifier find(DehumidifierId id) throws NotFoundException;
    }

    public static interface Api {
        void turnOn(DehumidifierId id, FanSpeed fanSpeed) throws ApiException;

        void turnOff(DehumidifierId id) throws ApiException;
    }

    public static enum FanSpeed {
        LOW, MID, MAX
    }

    public final DehumidifierId id;
    private final Api api;

    public void turnOn(FanSpeed fanSpeed) throws ApiException {
        api.turnOn(id, fanSpeed);
    }

    public void turnOff() throws ApiException {
        api.turnOff(id);
    }
}
