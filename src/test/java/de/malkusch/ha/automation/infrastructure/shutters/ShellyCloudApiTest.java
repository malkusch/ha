package de.malkusch.ha.automation.infrastructure.shutters;

import static de.malkusch.ha.automation.model.shutters.Shutter.Api.State.CLOSED;
import static de.malkusch.ha.automation.model.shutters.Shutter.Api.State.OPEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.malkusch.ha.automation.model.shutters.Shutter.Api;
import de.malkusch.ha.shared.infrastructure.http.HttpClient;
import de.malkusch.ha.shared.infrastructure.http.HttpClient.Field;
import de.malkusch.ha.shared.infrastructure.http.HttpResponse;

@ExtendWith(MockitoExtension.class)
public class ShellyCloudApiTest {

    @Mock
    private HttpClient http;

    private Api api;
    private static final String ANY_HOST = "http://example.org";

    @BeforeEach
    public void setup() {
        api = new ShellyCloudApi(ANY_HOST, "any", http, new ObjectMapper(), "terasse");
    }

    @ParameterizedTest
    @CsvSource({ "open,0", "open,100", "stop,100" })
    public void stateShouldReturnOpen(String state, int current_pos) throws Exception {
        when(http.post(eq(ANY_HOST + "/device/status"), any(Field[].class))).thenReturn(statusResponse(state, current_pos));

        var result = api.state();

        assertEquals(OPEN, result);
    }

    @ParameterizedTest
    @CsvSource({ "close,0", "close,100", "stop,0" })
    public void stateShouldReturnClosed(String state, int current_pos) throws Exception {
        when(http.post(eq(ANY_HOST + "/device/status"), any(Field[].class))).thenReturn(statusResponse(state, current_pos));

        var result = api.state();

        assertEquals(CLOSED, result);
    }

    @ParameterizedTest
    @CsvSource({ "stop,1", "stop,99" })
    public void stateShouldReturnHalfClosed(String state, int current_pos) throws Exception {
        when(http.post(eq(ANY_HOST + "/device/status"), any(Field[].class))).thenReturn(statusResponse(state, current_pos));

        var result = api.state();

        assertEquals(new Api.State(100 - current_pos), result);
    }

    @Test
    public void shouldOpen() throws Exception {
        when(http.post(eq(ANY_HOST + "/device/relay/roller/control"), any(Field[].class))).thenReturn(response("""
                {"isok":true,"data":{"device_id":"e8db84aaccd6"}}
                                """));

        api.setState(OPEN);

        verify(http).post(eq(ANY_HOST + "/device/relay/roller/control"), any(Field[].class));
    }

    @Test
    public void shouldClose() throws Exception {
        when(http.post(eq(ANY_HOST + "/device/relay/roller/control"), any(Field[].class))).thenReturn(response("""
                {"isok":true,"data":{"device_id":"e8db84aaccd6"}}
                """));

        api.setState(CLOSED);

        verify(http).post(eq(ANY_HOST + "/device/relay/roller/control"), any(Field[].class));
    }

    @Test
    public void shouldHalfOpen() throws Exception {
        when(http.post(eq(ANY_HOST + "/device/relay/roller/settings/topos"), any(Field[].class))).thenReturn(response("""
                {"isok":true,"data":{"device_id":"e8db84aaccd6"}}
                                """));

        api.setState(new Api.State(50));

        verify(http).post(eq(ANY_HOST + "/device/relay/roller/settings/topos"), any(Field[].class));
    }

    private static final String STATUS_TEMPLATE = """
            {
               "data" : {
                  "device_status" : {
                     "_updated" : "2021-11-09 06:23:47",
                     "actions_stats" : {
                        "skipped" : 0
                     },
                     "cfg_changed_cnt" : 0,
                     "cloud" : {
                        "connected" : true,
                        "enabled" : true
                     },
                     "fs_free" : 146333,
                     "fs_size" : 233681,
                     "getinfo" : {
                        "fw_info" : {
                           "device" : "shellyswitch25-E8DB84AACCD6",
                           "fw" : "20210909-144331/v1.11.4-DNSfix-ge6b2f6d"
                        }
                     },
                     "has_update" : false,
                     "inputs" : [
                        {
                           "event" : "",
                           "event_cnt" : 0,
                           "input" : 0
                        },
                        {
                           "event" : "",
                           "event_cnt" : 0,
                           "input" : 1
                        }
                     ],
                     "mac" : "E8DB84AACCD6",
                     "meters" : [
                        {
                           "counters" : [
                              0,
                              0,
                              0
                           ],
                           "is_valid" : true,
                           "overpower" : 0,
                           "power" : 0,
                           "timestamp" : 1636442627,
                           "total" : 1357
                        },
                        {
                           "counters" : [
                              0,
                              0,
                              0
                           ],
                           "is_valid" : true,
                           "overpower" : 0,
                           "power" : 0,
                           "timestamp" : 1636442627,
                           "total" : 1010
                        }
                     ],
                     "mqtt" : {
                        "connected" : false
                     },
                     "overtemperature" : false,
                     "ram_free" : 37352,
                     "ram_total" : 49936,
                     "rollers" : [
                        {
                           "calibrating" : false,
                           "current_pos" : ${current_pos},
                           "is_valid" : true,
                           "last_direction" : "open",
                           "overtemperature" : false,
                           "positioning" : true,
                           "power" : 0,
                           "safety_switch" : false,
                           "source" : "input",
                           "state" : "${state}",
                           "stop_reason" : "normal"
                        }
                     ],
                     "serial" : 1091,
                     "temperature" : 41.31,
                     "temperature_status" : "Normal",
                     "time" : "06:29",
                     "tmp" : {
                        "is_valid" : true,
                        "tC" : 41.31,
                        "tF" : 106.36
                     },
                     "unixtime" : 1636435756,
                     "update" : {
                        "beta_version" : "20211025-173450/v1.11.7-rc1-g239774e",
                        "has_update" : false,
                        "new_version" : "20210909-144331/v1.11.4-DNSfix-ge6b2f6d",
                        "old_version" : "20210909-144331/v1.11.4-DNSfix-ge6b2f6d",
                        "status" : "idle"
                     },
                     "uptime" : 43631,
                     "voltage" : 229.15,
                     "wifi_sta" : {
                        "connected" : true,
                        "ip" : "192.168.189.2",
                        "rssi" : -59,
                        "ssid" : "fuckshelly"
                     }
                  },
                  "online" : true
               },
               "isok" : true
            }
                        """;

    private static HttpResponse statusResponse(String state, int current_pos) {
        var substitor = new StringSubstitutor(Map.of("state", state, "current_pos", current_pos));
        return response(substitor.replace(STATUS_TEMPLATE));
    }

    private static HttpResponse response(String body) {
        return new HttpResponse(200, "http://example.org/", false, new ByteArrayInputStream(body.getBytes()));
    }
}
