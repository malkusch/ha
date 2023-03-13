package de.malkusch.ha.automation.infrastructure;

import static de.malkusch.ha.automation.model.State.OFF;
import static de.malkusch.ha.automation.model.State.ON;
import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.malkusch.ha.automation.model.State;
import de.malkusch.ha.shared.infrastructure.http.HttpClient;
import de.malkusch.ha.shared.model.ApiException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class TasmotaApi {

    private final String url;
    private final HttpClient http;
    private final ObjectMapper mapper;

    public State state() throws ApiException, InterruptedException {
        return parsePower(sendCommand("State"));
    }

    public void turnOn() throws ApiException, InterruptedException {
        var result = parsePower(sendCommand("Power On"));
        if (result != ON) {
            throw new ApiException("Failed turning on " + url + ": " + result);
        }
    }

    public void turnOff() throws ApiException, InterruptedException {
        var result = parsePower(sendCommand("Power Off"));
        if (result != OFF) {
            throw new ApiException("Failed turning off " + url + ": " + result);
        }
    }

    private static State parsePower(JsonNode response) throws ApiException {
        var power = response.path("POWER").textValue();
        return switch (power) {
        case "ON" -> ON;
        case "OFF" -> OFF;
        default -> throw new ApiException("Invalid response: " + response);
        };
    }

    private JsonNode sendCommand(String command) throws ApiException, InterruptedException {
        var commandUrl = url + "/cm?cmnd=" + encode(command, UTF_8);
        try (var response = http.get(commandUrl)) {
            return mapper.readTree(response.body);

        } catch (IOException e) {
            throw new ApiException("Failed to execute " + commandUrl, e);
        }
    }
}
