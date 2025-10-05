package de.malkusch.ha.automation.infrastructure.shutters;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.malkusch.ha.automation.model.shutters.Shutter.Api;
import de.malkusch.ha.automation.model.shutters.ShutterId;
import de.malkusch.ha.shared.infrastructure.http.JsonHttpExchange;
import de.malkusch.ha.shared.model.ApiException;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

import static de.malkusch.ha.automation.model.shutters.Shutter.Api.State.CLOSED;
import static de.malkusch.ha.automation.model.shutters.Shutter.Api.State.OPEN;

@RequiredArgsConstructor
public final class ShellyCloudV2Api implements Api {

    public interface Factory {
        Api build(ShutterId id, String deviceId);
    }

    private final String baseUri;
    private final String key;
    private final JsonHttpExchange http;
    private final String deviceId;

    @Override
    public void setState(State state) throws ApiException, InterruptedException {
        record OpenCloseRequest(String id, String position) {
        }

        record PositionRequest(String id, int position) {
        }

        Object request = switch (state) {
            case State s when s.equals(OPEN) -> new OpenCloseRequest(deviceId, "open");
            case State s when s.equals(CLOSED) -> new OpenCloseRequest(deviceId, "close");
            default -> new PositionRequest(deviceId, 100 - state.percent());
        };
        post("/v2/devices/api/set/cover", request);
    }

    @Override
    public State state() throws ApiException, InterruptedException {
        record StateRequest(String[] ids, String[] select) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        record StateResponse(Status status) {
            @JsonIgnoreProperties(ignoreUnknown = true)
            record Status(Rollers[] rollers) {
                @JsonIgnoreProperties(ignoreUnknown = true)
                record Rollers(String state, int current_pos) {
                }
            }
        }

        var request = new StateRequest(new String[]{deviceId}, new String[]{"status"});
        var response = post("/v2/devices/api/get", request, StateResponse[].class);
        var roller = response[0].status.rollers[0];

        return switch (roller.state) {
            case "open" -> OPEN;
            case "close" -> CLOSED;
            case "stop" -> new State(100 - roller.current_pos);
            default -> throw new IllegalStateException(String.format("Shutter %s is in state %s", deviceId, roller.state));
        };
    }

    private void post(String path, Object request) throws ApiException, InterruptedException {
        try {
            http.post(url(path), request);

        } catch (IOException e) {
            throw new ApiException("Failed " + path + " for " + deviceId, e);
        }
    }

    private <T> T post(String path, Object request, Class<T> responseType) throws ApiException, InterruptedException {
        try {
            var response = http.post(url(path), request, responseType);
            if (response.statusCode() != 200) {
                throw new ApiException(String.format("%s failed for %s [%s]:%s", path, deviceId, response.statusCode(), response.body()));
            }
            return response.body();

        } catch (IOException e) {
            throw new ApiException("Failed " + path + " for " + deviceId, e);
        }
    }

    private String url(String path) {
        return baseUri + path + "?auth_key=" + key;
    }
}
