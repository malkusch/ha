package de.malkusch.ha.automation.infrastructure.dehumidifier.midea;

import static de.malkusch.ha.automation.infrastructure.dehumidifier.midea.PacketBuilder.Device.DEHUMIDIFIER;
import static de.malkusch.ha.automation.infrastructure.dehumidifier.midea.PacketBuilder.PowerState.OFF;
import static de.malkusch.ha.automation.infrastructure.dehumidifier.midea.PacketBuilder.PowerState.ON;
import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Arrays.stream;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.codec.binary.Hex.encodeHexString;
import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;
import static org.apache.commons.lang3.ArrayUtils.add;
import static org.apache.commons.lang3.ArrayUtils.addAll;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.malkusch.ha.automation.model.ApiException;
import de.malkusch.ha.automation.model.Dehumidifier;
import de.malkusch.ha.automation.model.Dehumidifier.Api;
import de.malkusch.ha.automation.model.Dehumidifier.DehumidifierId;
import de.malkusch.ha.automation.model.Dehumidifier.FanSpeed;
import de.malkusch.ha.shared.infrastructure.http.HttpClient;
import de.malkusch.ha.shared.infrastructure.http.HttpClient.Field;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class MideaApi implements Api {

    private final String appKey;
    private final String loginAccount;
    private final String encryptedPassword;
    private final Field[] requestParameters;
    private final HttpClient http;
    private final ObjectMapper mapper;
    private volatile Session session;

    private static class Session {
        public String sessionId;
        public String accessToken;
        public Encryption encryption;
    }

    public MideaApi(String appKey, String loginAccount, String password, Field[] requestParameters, HttpClient http,
            ObjectMapper mapper) throws ApiException, InterruptedException {

        this.appKey = appKey;
        this.loginAccount = loginAccount;
        this.requestParameters = requestParameters;
        this.http = http;
        this.mapper = mapper;
        this.encryptedPassword = new Initializer().encryptedPassword(password);

        login();
    }

    private class Initializer {
        private String encryptedPassword(String password) throws ApiException, InterruptedException {
            var url = "https://mapp.appsmb.com/v1/user/login/id/get";
            var loginId = apiRequest(url, new Field("loginAccount", loginAccount)).get("loginId").textValue();
            var passwordHash = sha256Hex(password);
            return sha256Hex(loginId + passwordHash + appKey);
        }
    }

    @Override
    public void turnOn(DehumidifierId id, FanSpeed fanSpeed) throws ApiException, InterruptedException {
        send(id, new PacketBuilder(DEHUMIDIFIER).setPowerState(ON).build());
    }

    @Override
    public void turnOff(DehumidifierId id) throws ApiException, InterruptedException {
        send(id, new PacketBuilder(DEHUMIDIFIER).setPowerState(OFF).build());
    }

    private void send(DehumidifierId id, byte[] data) throws ApiException, InterruptedException {
        var url = "https://mapp.appsmb.com/v1/appliance/transparent/send";
        var order = session.encryption.encrypt(data);
        apiRequest(url, new Field("order", encodeHexString(order)), new Field("funId", "0000"),
                new Field("applianceId", id.getId()), sessionId());
    }

    public Stream<Dehumidifier> detect() throws ApiException, InterruptedException {
        return homegroups().stream().flatMap(this::list).map(it -> new Dehumidifier(it, this));
    }

    @SneakyThrows
    private Stream<DehumidifierId> list(String homegroupId) {
        var url = "https://mapp.appsmb.com/v1/appliance/list/get";

        var appliances = apiRequest(url, new Field("homegroupId", homegroupId), sessionId()).get("list")
                .findValuesAsText("id");
        return appliances.stream().map(DehumidifierId::new);
    }

    private List<String> homegroups() throws ApiException, InterruptedException {
        var url = "https://mapp.appsmb.com/v1/homegroup/list/get";
        return apiRequest(url, sessionId()).get("list").findValuesAsText("id");
    }

    private void login() throws ApiException, InterruptedException {
        var url = "https://mapp.appsmb.com/v1/user/login";
        var json = apiRequest(url, new Field("loginAccount", loginAccount), new Field("password", encryptedPassword));
        var session = mapper.convertValue(json, Session.class);
        session.encryption = new Encryption(appKey, session.accessToken);
        this.session = session;
        log.debug("Logged in: {}", json);
    }

    private JsonNode apiRequest(String url, Field... fields) throws ApiException, InterruptedException {
        var stamp = new Field("stamp", now().format(ofPattern("yyyyMMddHHmmss")));
        var effectiveParameters = addAll(requestParameters, add(fields, stamp));

        var sign = new Field("sign", sign(url, effectiveParameters));
        var signedParameters = add(effectiveParameters, sign);

        try (var response = http.post(url, signedParameters)) {
            var json = mapper.readTree(response.body);

            var error = ofNullable(json.get("errorCode")).map(JsonNode::intValue).orElse(0);
            if (error != 0) {
                throw new ApiException(
                        "Failed to call " + url + Arrays.toString(signedParameters) + ": " + json.toPrettyString());
            }

            var result = json.get("result");
            if (result == null) {
                throw new ApiException(
                        "Failed to call " + url + Arrays.toString(signedParameters) + ": " + json.toPrettyString());
            }

            return result;

        } catch (IOException e) {
            throw new ApiException("Failed to call " + url, e);
        }
    }

    private String sign(String url, Field[] fields) {
        var path = URI.create(url).getPath();

        var sorted = stream(fields).sorted(comparing(it -> it.name));
        var sortedQuery = sorted.map(it -> it.name + "=" + it.value).collect(joining("&"));

        return sha256Hex(path + sortedQuery + appKey);
    }

    private Field sessionId() throws ApiException, InterruptedException {
        if (session == null) {
            login();
        }
        return new Field("sessionId", session.sessionId);
    }
}
