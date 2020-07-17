package de.malkusch.ha.automation.infrastructure.dehumidifier.midea_python;

import static de.malkusch.ha.automation.infrastructure.dehumidifier.midea_python.Session.Command.FAN_SPEED;
import static de.malkusch.ha.automation.infrastructure.dehumidifier.midea_python.Session.Command.OFF;
import static de.malkusch.ha.automation.infrastructure.dehumidifier.midea_python.Session.Command.ON;

import java.util.Map;

import de.malkusch.ha.automation.model.ApiException;
import de.malkusch.ha.automation.model.dehumidifier.Dehumidifier.Api;
import de.malkusch.ha.automation.model.dehumidifier.Dehumidifier.DehumidifierId;
import de.malkusch.ha.automation.model.dehumidifier.Dehumidifier.FanSpeed;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class PythonMideaApi implements Api {

    private final String loginAccount;
    private final String password;
    private final String path;

    private static final Map<FanSpeed, String> FAN_SPEEDS = Map.of( //
            FanSpeed.LOW, "40", //
            FanSpeed.MID, "60", //
            FanSpeed.MAX, "80");

    @Override
    public void turnOn(DehumidifierId id, FanSpeed fanSpeed) throws ApiException, InterruptedException {
        try (var session = session(id)) {
            session.enter(ON);
            session.enter(FAN_SPEED, FAN_SPEEDS.get(fanSpeed));
        }
    }

    @Override
    public void turnOff(DehumidifierId id) throws ApiException, InterruptedException {
        try (var session = session(id)) {
            session.enter(OFF);
        }
    }

    private Session session(DehumidifierId id) throws ApiException {
        return new Session(id, loginAccount, password, path);
    }
}
