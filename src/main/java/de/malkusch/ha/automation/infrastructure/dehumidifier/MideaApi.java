package de.malkusch.ha.automation.infrastructure.dehumidifier;

import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ofPattern;
import static org.apache.commons.lang3.ArrayUtils.add;
import static org.apache.commons.lang3.ArrayUtils.addAll;

import java.io.IOException;
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

final class MideaApi implements Api {

    private final String appKey;
    private final String loginAccount;
    private final String password;
    private final Field[] requestParameters;
    private final HttpClient http;
    private final ObjectMapper mapper;

    public MideaApi(String appKey, String loginAccount, String password, Field[] requestParameters, HttpClient http,
            ObjectMapper mapper) {

        this.appKey = appKey;
        this.loginAccount = loginAccount;
        this.password = password;
        this.requestParameters = requestParameters;
        this.http = http;
        this.mapper = mapper;
    }

    @Override
    public void turnOn(DehumidifierId id, FanSpeed fanSpeed) throws ApiException {
        // TODO Auto-generated method stub

    }

    @Override
    public void turnOff(DehumidifierId id) throws ApiException {
        // TODO Auto-generated method stub

    }

    Stream<Dehumidifier> detect() throws ApiException, InterruptedException {
        return homegroups().stream().flatMap(this::list).map(it -> new Dehumidifier(it, this));
    }

    @SneakyThrows
    private Stream<DehumidifierId> list(String homegroupId) {
        var url = "https://mapp.appsmb.com/v1/appliance/list/get";
        return apiRequest(url, new Field("homegroupId", homegroupId)).findValues("list").stream()
                .map(it -> new DehumidifierId(it.get("id").asText()));
    }

    private List<String> homegroups() throws ApiException, InterruptedException {
        var url = "https://mapp.appsmb.com/v1/homegroup/list/get";
        return apiRequest(url).findValuesAsText("list");
    }

    private String loginId() throws ApiException, InterruptedException {
        var url = "https://mapp.appsmb.com/v1/user/login/id/get";
        return apiRequest(url, new Field("loginAccount", loginAccount)).get("loginId").textValue();
    }

    private String accessToken() throws ApiException, InterruptedException {
        var url = "https://mapp.appsmb.com/v1/user/login";
        var loginId = loginId();
        var encrypted = encrypt(loginId, password);
        return apiRequest(url, new Field("loginAccount", loginAccount), new Field("password", encrypted))
                .get("accessToken").textValue();
    }

    private static String encrypt(String loginId, String password) {
        // TODO
        return "9d6b91fe6982d65c45a53671362c6c40660473202ac6a6bb54bbf63a9fabf82d";
    }

    private JsonNode apiRequest(String url, Field... fields) throws ApiException, InterruptedException {
        var stamp = new Field("stamp", now().format(ofPattern("yyyyMMddHHmmss")));
        Field[] effectiveParameters = addAll(requestParameters, add(fields, stamp));
        try (var response = http.post(url, effectiveParameters)) {
            var json = mapper.readTree(response.body);
            if (json.has("errorCode")) {
                throw new ApiException(
                        "Failed to call " + url + Arrays.toString(effectiveParameters) + ": " + json.toPrettyString());
            }
            return json;

        } catch (IOException e) {
            throw new ApiException("Failed to call " + url, e);
        }
    }
}
