package de.malkusch.ha.automation.infrastructure.shutters;

import static de.malkusch.ha.automation.model.shutters.Shutter.Api.State.CLOSED;
import static de.malkusch.ha.automation.model.shutters.Shutter.Api.State.OPEN;

import java.io.IOException;

import org.apache.commons.lang3.ArrayUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.RateLimiter;

import de.malkusch.ha.automation.model.shutters.Shutter.Api;
import de.malkusch.ha.shared.infrastructure.http.HttpClient;
import de.malkusch.ha.shared.infrastructure.http.HttpClient.Field;
import de.malkusch.ha.shared.model.ApiException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class ShellyCloudApi implements Api {

    private final String baseUri;
    private final String key;
    private final HttpClient http;
    private final ObjectMapper mapper;
    private final String deviceId;
    private final RateLimiter rateLimiter;

    @Override
    public void setState(State state) throws ApiException, InterruptedException {
        if (state.equals(OPEN)) {
            post("/device/relay/roller/control", direction("open"));
        } else if (state.equals(CLOSED)) {
            post("/device/relay/roller/control", direction("close"));
        } else {
            post("/device/relay/roller/settings/topos", new Field("pos", 100 - state.percent()));
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class StatusResponse extends Response {

        public Data data;

        @JsonIgnoreProperties(ignoreUnknown = true)
        private static record Data(DeviceStatus device_status) {

            @JsonIgnoreProperties(ignoreUnknown = true)
            private static record DeviceStatus(Roller[] rollers) {

                @JsonIgnoreProperties(ignoreUnknown = true)
                private static record Roller(int current_pos, String state) {
                }
            }
        }
    }

    @Override
    public State state() throws ApiException, InterruptedException {
        var status = post("/device/status", StatusResponse.class);
        var roller = status.data.device_status.rollers[0];

        return switch (roller.state) {
        case "open" -> OPEN;
        case "close" -> CLOSED;
        case "stop" -> new State(100 - roller.current_pos);
        default -> throw new IllegalStateException(String.format("Shutter %s is in state %s", deviceId, roller.state));
        };
    }

    private static Field direction(String direction) {
        return new Field("direction", direction);
    }

    private void post(String path, Field... fields) throws ApiException, InterruptedException {
        post(path, Response.class, fields);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Response {
        public boolean isok;
        public String errors;
    }

    private <T extends Response> T post(String path, Class<T> responseType, Field... fields)
            throws ApiException, InterruptedException {

        var withAuthentication = ArrayUtils
                .addAll(new Field[] { new Field("id", deviceId), new Field("auth_key", key) }, fields);

        rateLimiter.acquire();
        try (var httpResponse = http.post(baseUri + path, withAuthentication)) {
            if (httpResponse.statusCode != 200) {
                throw new ApiException(String.format("%s failed for %s [%s]:%s", path, deviceId,
                        httpResponse.statusCode, httpResponse.bodyAsString()));
            }
            var response = mapper.readValue(httpResponse.body, responseType);
            if (!response.isok) {
                throw new ApiException(String.format("Failed %s for id %s: %s", path, deviceId, response.errors));
            }
            return response;

        } catch (IOException e) {
            throw new ApiException("Failed " + path + " for " + deviceId, e);
        }
    }
}
