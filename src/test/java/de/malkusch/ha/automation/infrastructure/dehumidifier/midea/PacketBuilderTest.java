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
                "5a5a01115700200000000000000000000e031214c679000000050a00000000000000000002000000aa1ea100000000000302408000ff03ff00300000000000000000000003bb8d00000000000000000000000000000000",
                encodeHexString(data));
    }

    @Test
    public void shouldTurnOnHumidifier() {
        var data = new PacketBuilder(DEHUMIDIFIER).setPowerState(ON).build();

        assertEquals(
                "5a5a01115700200000000000000000000e031214c679000000050a00000000000000000002000000aa1ea100000000000302408100ff03ff00300000000000000000000003bb8c00000000000000000000000000000000",
                encodeHexString(data));
    }

}
