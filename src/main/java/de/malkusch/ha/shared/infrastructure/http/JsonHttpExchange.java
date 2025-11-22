package de.malkusch.ha.shared.infrastructure.http;

import de.malkusch.ha.shared.infrastructure.http.HttpClient.Header;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public final class JsonHttpExchange {

    private final HttpClient http;
    private final ObjectMapper mapper;

    public record JsonHttpResponse<T>(int statusCode, T body) {
    }

    private static final Header HEADER_JSON = new Header("Content-Type", "application/json");

    public void post(String url, Object request) throws IOException, InterruptedException {
        var body = mapper.writeValueAsString(request);
        try (var response = http.post(url, body, HEADER_JSON)) {
        }
    }

    public <T> JsonHttpResponse<T> post(String url, Object request, Class<T> responseType) throws IOException, InterruptedException {
        var body = mapper.writeValueAsString(request);
        try (var response = http.post(url, body, HEADER_JSON)) {
            return new JsonHttpResponse<>(response.statusCode, mapper.readValue(response.body, responseType));
        }
    }
}
