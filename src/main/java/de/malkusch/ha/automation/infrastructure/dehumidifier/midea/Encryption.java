package de.malkusch.ha.automation.infrastructure.dehumidifier.midea;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.util.Arrays.copyOfRange;
import static java.util.Arrays.fill;
import static javax.crypto.Cipher.DECRYPT_MODE;
import static javax.crypto.Cipher.ENCRYPT_MODE;
import static org.apache.commons.codec.binary.Hex.decodeHex;
import static org.apache.commons.codec.digest.DigestUtils.md5Hex;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
final class Encryption {

    private static final IvParameterSpec IV = new IvParameterSpec(
            new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
    private static final int BLOCK = 16;

    private final SecretKeySpec key;

    public Encryption(String appKey, String accessToken) {
        this(key(appKey, accessToken));
    }

    @SneakyThrows
    static SecretKeySpec key(String appKey, String accessToken) {
        var hash = md5Hex(appKey.getBytes(US_ASCII)).getBytes(US_ASCII);
        var key = key(hash);
        var encryption = new Encryption(key);
        return key(encryption.decrypt(decodeHex(accessToken)));
    }

    private static SecretKeySpec key(byte[] key) {
        return new SecretKeySpec(key, 0, BLOCK, "AES");
    }

    @SneakyThrows
    public byte[] decrypt(byte[] data) {
        return perform(DECRYPT_MODE, data);
    }

    @SneakyThrows
    public byte[] encrypt(byte[] data) {
        return perform(ENCRYPT_MODE, data);
    }

    @SneakyThrows
    private byte[] perform(int mode, byte[] data) {
        var padded = pad(data);
        var cipher = Cipher.getInstance("AES/CBC/NoPadding");
        for (var i = 0; i < padded.length; i += BLOCK) {
            cipher.init(mode, key, IV);
            cipher.doFinal(padded, i, BLOCK, padded, i);
        }
        return padded;
    }

    private static byte[] pad(byte[] data) {
        var padding = (BLOCK - data.length % BLOCK) % BLOCK;
        var padded = copyOfRange(data, 0, data.length + padding);
        fill(padded, data.length, padded.length, (byte) padding);
        return padded;
    }
}
