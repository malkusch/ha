package de.malkusch.ha.automation.infrastructure.dehumidifier.midea;

import static java.lang.System.arraycopy;
import static java.util.Arrays.copyOf;
import static java.util.Arrays.copyOfRange;

import lombok.RequiredArgsConstructor;

//See https://github.com/lismarc/midea-openhab/
final class PacketBuilder {

    private final byte[] command = { //
            (byte) 0xaa, 0x23, (byte) 0xAC, 0x00, 0x00, 0x00, 0x00, 0x00, //
            0x03, 0x02, 0x40, (byte) 0x81, 0x00, (byte) 0xff, 0x03, (byte) 0xff, //
            0x00, 0x30, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, //
            0x00, 0x00, 0x00, 0x00, 0x03, (byte) 0xcc //
    };

    @RequiredArgsConstructor
    static enum Device {
        AC((byte) 0xAC), DEHUMIDIFIER((byte) 0xA1);

        final byte value;
    }

    PacketBuilder(Device device) {
        command[0x02] = device.value;
    }

    @RequiredArgsConstructor
    static enum PowerState {
        ON((byte) 0x81), OFF((byte) 0x80);

        final byte value;
    }

    PacketBuilder setPowerState(PowerState state) {
        command[0x0b] = state.value;
        return this;
    }

    private byte[] buildCommand() {
        command[0x1d] = crc8(command, 16);
        command[0x01] = (byte) command.length;
        return command;
    }

    private static byte crc8(byte[] data, int start) {
        var checkedData = copyOfRange(data, start, data.length);
        return (byte) CRC8_Maxim.getInstance().compute(checkedData);
    }

    private static final byte[] HEADER = { //
            0x5a, 0x5a, 0x01, 0x11, 0x5c, 0x00, 0x20, 0x00, //
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, //
            0x0e, 0x03, 0x12, 0x14, (byte) 0xc6, 0x79, 0x00, 0x00, //
            0x00, 0x05, 0x0a, 0x00, 0x00, 0x00, 0x00, 0x00, //
            0x00, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00 //
    };

    byte[] build() {
        var command = buildCommand();
        var packet = copyOf(HEADER, HEADER.length + 47);
        arraycopy(command, 0, packet, HEADER.length, command.length);

        packet[HEADER.length + command.length] = packetChecksum(command, 1);
        packet[0x04] = (byte) packet.length;
        return packet;
    }

    private static byte packetChecksum(byte[] data, int start) {
        var sum = 0;
        for (int i = start; i < data.length; i++) {
            sum += data[i];
        }
        return (byte) (255 - sum % 256 + 1);
    }
}
