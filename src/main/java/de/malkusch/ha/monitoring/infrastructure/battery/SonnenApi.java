package de.malkusch.ha.monitoring.infrastructure.battery;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.malkusch.ha.shared.infrastructure.http.HttpClient;

@Service
public final class SonnenApi {

    private final String url;
    private final HttpClient http;
    private final ObjectMapper mapper;

    SonnenApi(@Value("${sonnen.url}") String url, HttpClient http, ObjectMapper mapper) {
        this.url = url;
        this.http = http;
        this.mapper = mapper;
    }

    public static final class Status {
        public int Consumption_W;
        public int Production_W;
        public int USOC;
        public int GridFeedIn_W;
        public int Pac_total_W;
        public int Ubat;
        public int Uac;
        public int RemainingCapacity_W;
        public int RSOC;
        public int Sac1;
        public int Sac2;
        public int Sac3;
    }

    public Status status() throws IOException, InterruptedException {
        try (var response = http.get(url)) {
            return mapper.readValue(response.body, Status.class);
        }
    }
}
