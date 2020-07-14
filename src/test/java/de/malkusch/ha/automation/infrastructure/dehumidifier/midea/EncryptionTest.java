package de.malkusch.ha.automation.infrastructure.dehumidifier.midea;

import static de.malkusch.ha.automation.infrastructure.dehumidifier.midea.Encryption.key;
import static de.malkusch.ha.automation.infrastructure.dehumidifier.midea.PacketBuilder.Device.DEHUMIDIFIER;
import static de.malkusch.ha.automation.infrastructure.dehumidifier.midea.PacketBuilder.PowerState.OFF;
import static de.malkusch.ha.automation.infrastructure.dehumidifier.midea.PacketBuilder.PowerState.ON;
import static org.apache.commons.codec.binary.Hex.encodeHexString;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class EncryptionTest {

    private final String appKey = "3742e9e5842d4ad59c2db887e12449f9";
    private final String accessToken = "63659dc91df9de89ecbb5596962fae3eb920ebb829d567559397ded751813801";

    @Test
    public void shouldBuildKey() {
        var key = key(appKey, accessToken);
        assertEquals("64383830326163323166303234316163", encodeHexString(key.getEncoded()));
    }

    @Test
    public void shouldEncryptTurnOn() throws Exception {
        var encryption = new Encryption(appKey, accessToken);
        var encodedCommand = new PacketBuilder(DEHUMIDIFIER).setPowerState(ON).build();

        var order = encryption.encrypt(encodedCommand);

        assertEquals(
                "ad3433d37a91074af305b71152e464284dc3d3e6c8b453030e043e97cc30a1ecd581e5081aebc8da02240174c7665b86052a02c86f3a80a72fec888726d9e86cd8094c98eedcd0c3636ff69c3543f2737df69b8da76d47bca3c973b1745d74eb3cde1587bc8eb7e2e1aee71df34a7d0e225a758268f875a34db843b7533c8eadfd6115a5f4425361691693bd529e4ee406af453e78421e5f955e80208a6b05419605d9c99d2f4af9f269341d8b6eff51ab16d53a93b3a0e89182b21300156bb6bba61a470c2c34583b729b011603c2cf",
                encodeHexString(order));
    }

    @Test
    public void shouldEncryptTurnOff() throws Exception {
        var encryption = new Encryption(appKey, accessToken);
        var encodedCommand = new PacketBuilder(DEHUMIDIFIER).setPowerState(OFF).build();

        var order = encryption.encrypt(encodedCommand);

        assertEquals(
                "ad3433d37a91074af305b71152e464284dc3d3e6c8b453030e043e97cc30a1ecd581e5081aebc8da02240174c7665b86052a02c86f3a80a72fec888726d9e86cd8094c98eedcd0c3636ff69c3543f2737df69b8da76d47bca3c973b1745d74eb3cde1587bc8eb7e2e1aee71df34a7d0e9db3112d61eeb841a223cf8a6fe0a6a1fd6115a5f4425361691693bd529e4ee406af453e78421e5f955e80208a6b0541e46a5b41f80e78aac7f3f3b10bbd70bcab16d53a93b3a0e89182b21300156bb6bba61a470c2c34583b729b011603c2cf",
                encodeHexString(order));
    }
}
