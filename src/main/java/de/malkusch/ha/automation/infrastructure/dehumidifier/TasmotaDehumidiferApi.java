package de.malkusch.ha.automation.infrastructure.dehumidifier;

import de.malkusch.ha.automation.infrastructure.TasmotaApi;
import de.malkusch.ha.automation.model.ApiException;
import de.malkusch.ha.automation.model.State;
import de.malkusch.ha.automation.model.dehumidifier.Dehumidifier.Api;
import de.malkusch.ha.automation.model.dehumidifier.Dehumidifier.DehumidifierId;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class TasmotaDehumidiferApi implements Api {

    private final TasmotaApi tasmota;

    @Override
    public State state() throws ApiException, InterruptedException {
        return tasmota.state();
    }

    @Override
    public void turnOn(DehumidifierId id) throws ApiException, InterruptedException {
        tasmota.turnOn();
    }

    @Override
    public void turnOff(DehumidifierId id) throws ApiException, InterruptedException {
        tasmota.turnOff();
    }
}
