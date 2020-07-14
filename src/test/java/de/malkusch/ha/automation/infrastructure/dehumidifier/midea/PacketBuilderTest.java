package de.malkusch.ha.automation.infrastructure.dehumidifier.midea;

import static de.malkusch.ha.automation.infrastructure.dehumidifier.midea.PacketBuilder.Device.DEHUMIDIFIER;
import static de.malkusch.ha.automation.infrastructure.dehumidifier.midea.PacketBuilder.PowerState.OFF;
import static de.malkusch.ha.automation.infrastructure.dehumidifier.midea.PacketBuilder.PowerState.ON;
import static org.apache.commons.codec.binary.Hex.encodeHexString;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class PacketBuilderTest {

    @Test
    public void shouldTurnOffHumidifier() {
        var data = new PacketBuilder(DEHUMIDIFIER).setPowerState(OFF).build();

        assertEquals(
                "39302c39302c312c31372c38372c302c33322c302c302c302c302c302c302c302c302c302c31342c332c31382c32302c2d35382c3132312c302c302c302c352c31302c302c302c302c302c302c302c302c302c302c322c302c302c302c2d38362c33302c2d39352c302c302c302c302c302c332c322c36342c2d3132382c302c2d312c332c2d312c302c34382c302c302c302c302c302c302c302c302c302c302c332c2d36392c2d3131352c302c302c302c302c302c302c302c302c302c302c302c302c302c302c302c30",
                encodeHexString(data));
    }

    @Test
    public void shouldTurnOnHumidifier() {
        var data = new PacketBuilder(DEHUMIDIFIER).setPowerState(ON).build();

        assertEquals(
                "39302c39302c312c31372c38372c302c33322c302c302c302c302c302c302c302c302c302c31342c332c31382c32302c2d35382c3132312c302c302c302c352c31302c302c302c302c302c302c302c302c302c302c322c302c302c302c2d38362c33302c2d39352c302c302c302c302c302c332c322c36342c2d3132372c302c2d312c332c2d312c302c34382c302c302c302c302c302c302c302c302c302c302c332c2d36392c2d3131362c302c302c302c302c302c302c302c302c302c302c302c302c302c302c302c30",
                encodeHexString(data));
    }

}
