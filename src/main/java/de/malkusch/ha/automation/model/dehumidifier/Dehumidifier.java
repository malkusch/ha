package de.malkusch.ha.automation.model.dehumidifier;

import static de.malkusch.ha.automation.model.State.OFF;
import static de.malkusch.ha.automation.model.State.ON;
import static java.time.Duration.ofMinutes;

import java.util.Collection;

import de.malkusch.ha.automation.model.NotFoundException;
import de.malkusch.ha.automation.model.State;
import de.malkusch.ha.automation.model.electricity.Watt;
import de.malkusch.ha.automation.model.room.RoomId;
import de.malkusch.ha.shared.infrastructure.CoolDown;
import de.malkusch.ha.shared.infrastructure.CoolDown.CoolDownException;
import de.malkusch.ha.shared.model.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public final class Dehumidifier {

    @Value
    public static final class DehumidifierId {
        private final String id;

        @Override
        public String toString() {
            return id;
        }
    }

    public static interface DehumidifierRepository {
        Dehumidifier find(DehumidifierId id) throws NotFoundException;

        Collection<Dehumidifier> findAll();
    }

    public static interface Api {
        State state() throws ApiException, InterruptedException;

        void turnOn(DehumidifierId id) throws ApiException, InterruptedException;

        void turnOff(DehumidifierId id) throws ApiException, InterruptedException;
    }

    public final DehumidifierId id;
    public final RoomId room;
    public final Watt power;
    public final DesiredHumidity desiredHumidity;
    private final Api api;
    private final CoolDown coolDown = new CoolDown(ofMinutes(5));

    public void turnOn() throws ApiException, InterruptedException, CoolDownException {
        coolDown.<ApiException, InterruptedException> withCoolDown(() -> {
            api.turnOn(id);
            if (state() != ON) {
                throw new ApiException(id + " is not on");
            }
            log.info("Dehumidier {} turned on", id);
        });
    }

    public void turnOff() throws ApiException, InterruptedException, CoolDownException {
        coolDown.<ApiException, InterruptedException> withCoolDown(() -> {
            api.turnOff(id);
            if (state() != OFF) {
                throw new ApiException(id + " is not off");
            }
            log.info("Dehumidier {} turned off", id);
        });
    }

    public State state() throws ApiException, InterruptedException {
        return api.state();
    }

    @Override
    public String toString() {
        return id.toString();
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return switch (obj) {
        case Dehumidifier other -> other.id.equals(id);
        default -> false;
        };
    }
}
