package de.malkusch.ha.automation.infrastructure.shutters;

import static de.malkusch.ha.automation.model.shutters.Shutter.Api.State.CLOSED;
import static de.malkusch.ha.automation.model.shutters.Shutter.Api.State.HALF_CLOSED;
import static de.malkusch.ha.automation.model.shutters.Shutter.Api.State.OPEN;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.malkusch.ha.automation.model.shutters.Shutter.Api;
import de.malkusch.ha.automation.model.shutters.ShutterId;
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
    private final Map<ShutterId, String> deviceIds;

    @Override
    public void open(ShutterId id) throws ApiException, InterruptedException {
        post("/device/relay/roller/control", id, direction("open"));
    }

    @Override
    public void close(ShutterId id) throws ApiException, InterruptedException {
        post("/device/relay/roller/control", id, direction("close"));
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
    public State state(ShutterId id) throws ApiException, InterruptedException {
        var status = post("/device/status", id, StatusResponse.class);
        var roller = status.data.device_status.rollers[0];

        return switch (roller.state) {
        case "open" -> OPEN;
        case "close" -> CLOSED;
        case "stop" -> switch (roller.current_pos) {
            case 100 -> OPEN;
            case 0 -> CLOSED;
            default -> HALF_CLOSED;
            };
        default -> throw new IllegalStateException(String.format("Shutter %s is in state %s", id, roller.state));
        };
    }

    private static Field direction(String direction) {
        return new Field("direction", direction);
    }

    private void post(String path, ShutterId id, Field... fields) throws ApiException, InterruptedException {
        post(path, id, Response.class, fields);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Response {
        public boolean isok;
        public String errors;
    }

    private <T extends Response> T post(String path, ShutterId id, Class<T> responseType, Field... fields)
            throws ApiException, InterruptedException {

        var deviceId = deviceIds.get(id);
        var withAuthentication = ArrayUtils
                .addAll(new Field[] { new Field("id", deviceId), new Field("auth_key", key) }, fields);

        try (var httpResponse = http.post(baseUri + path, withAuthentication)) {
            if (httpResponse.statusCode != 200) {
                throw new ApiException("Failed opening " + id + " with status " + httpResponse.statusCode);
            }
            var response = mapper.readValue(httpResponse.body, responseType);
            if (!response.isok) {
                throw new ApiException(String.format("Failed %s for id %s: %s", path, id, response.errors));
            }
            return response;

        } catch (IOException e) {
            throw new ApiException("Failed " + path + " for " + id, e);
        }
    }
}
