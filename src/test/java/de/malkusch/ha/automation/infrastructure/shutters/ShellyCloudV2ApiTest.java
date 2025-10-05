package de.malkusch.ha.automation.infrastructure.shutters;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.malkusch.ha.automation.model.shutters.Shutter.Api;
import de.malkusch.ha.shared.infrastructure.http.HttpClient;
import de.malkusch.ha.shared.infrastructure.http.HttpClient.Header;
import de.malkusch.ha.shared.infrastructure.http.HttpResponse;
import de.malkusch.ha.shared.infrastructure.http.JsonHttpExchange;
import org.apache.commons.text.StringSubstitutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.util.Map;

import static de.malkusch.ha.automation.model.shutters.Shutter.Api.State.CLOSED;
import static de.malkusch.ha.automation.model.shutters.Shutter.Api.State.OPEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ShellyCloudV2ApiTest {

    @Mock
    private HttpClient http;

    private Api api;
    private static final String ANY_HOST = "http://example.org";

    @BeforeEach
    public void setup() {
        api = new ShellyCloudV2Api(ANY_HOST, "any", new JsonHttpExchange(http, new ObjectMapper()), "terrasse");
    }

    @ParameterizedTest
    @CsvSource({"open,0", "open,100", "stop,100"})
    public void stateShouldReturnOpen(String state, int current_pos) throws Exception {
        when(http.post(eq(ANY_HOST + "/v2/devices/api/get?auth_key=any"), anyString(), any(Header.class))).thenReturn(statusResponse(state, current_pos));

        var result = api.state();

        assertEquals(OPEN, result);
    }

    @ParameterizedTest
    @CsvSource({"close,0", "close,100", "stop,0"})
    public void stateShouldReturnClosed(String state, int current_pos) throws Exception {
        when(http.post(eq(ANY_HOST + "/v2/devices/api/get?auth_key=any"), anyString(), any(Header.class))).thenReturn(statusResponse(state, current_pos));

        var result = api.state();

        assertEquals(CLOSED, result);
    }

    @ParameterizedTest
    @CsvSource({"stop,1", "stop,99"})
    public void stateShouldReturnHalfClosed(String state, int current_pos) throws Exception {
        when(http.post(eq(ANY_HOST + "/v2/devices/api/get?auth_key=any"), anyString(), any(Header.class))).thenReturn(statusResponse(state, current_pos));

        var result = api.state();

        assertEquals(new Api.State(100 - current_pos), result);
    }

    @Test
    public void shouldOpen() throws Exception {
        when(http.post(eq(ANY_HOST + "/v2/devices/api/set/cover?auth_key=any"), anyString(), any(Header.class))).thenReturn(response(""));

        api.setState(OPEN);

        verify(http).post(eq(ANY_HOST + "/v2/devices/api/set/cover?auth_key=any"), anyString(), any(Header.class));
    }

    @Test
    public void shouldClose() throws Exception {
        when(http.post(eq(ANY_HOST + "/v2/devices/api/set/cover?auth_key=any"), anyString(), any(Header.class))).thenReturn(response(""));

        api.setState(CLOSED);

        verify(http).post(eq(ANY_HOST + "/v2/devices/api/set/cover?auth_key=any"), anyString(), any(Header.class));
    }

    @Test
    public void shouldHalfOpen() throws Exception {
        when(http.post(eq(ANY_HOST + "/v2/devices/api/set/cover?auth_key=any"), anyString(), any(Header.class))).thenReturn(response(""));

        api.setState(new Api.State(50));

        verify(http).post(eq(ANY_HOST + "/v2/devices/api/set/cover?auth_key=any"), anyString(), any(Header.class));
    }

    private static final String STATUS_TEMPLATE = """
            [
               {
                  "online" : 1,
                  "type" : "relay",
                  "gen" : "G1",
                  "id" : "3c6105e5676d",
                  "code" : "SHSW-25",
                  "status" : {
                     "mqtt" : {
                        "connected" : false
                     },
                     "update" : {
                        "old_version" : "20230913-112234/v1.14.0-gcb84623",
                        "new_version" : "20230913-112234/v1.14.0-gcb84623",
                        "status" : "idle",
                        "has_update" : false,
                        "beta_version" : "20231107-163214/v1.14.1-rc1-g0617c15"
                     },
                     "mac" : "3C6105E5676D",
                     "temperature" : 44.27,
                     "has_update" : false,
                     "wifi_sta" : {
                        "connected" : true,
                        "ip" : "192.168.189.7",
                        "rssi" : -68,
                        "ssid" : "fuckshelly"
                     },
                     "uptime" : 30790,
                     "inputs" : [
                        {
                           "event_cnt" : 0,
                           "event" : "",
                           "input" : 0
                        },
                        {
                           "event" : "",
                           "event_cnt" : 0,
                           "input" : 0
                        }
                     ],
                     "getinfo" : {
                        "fw_info" : {
                           "device" : "shellyswitch25-3C6105E5676D",
                           "fw" : "20230913-112234/v1.14.0-gcb84623"
                        }
                     },
                     "ram_free" : 38296,
                     "actions_stats" : {
                        "skipped" : 0
                     },
                     "time" : "19:17",
                     "serial" : 1066,
                     "overtemperature" : false,
                     "_updated" : "2025-10-05 21:16:55",
                     "cloud" : {
                        "enabled" : true,
                        "connected" : true
                     },
                     "fs_size" : 233681,
                     "unixtime" : 1759684669,
                     "cfg_changed_cnt" : 2,
                     "meters" : [
                        {
                           "overpower" : 0,
                           "counters" : [
                              0,
                              0,
                              0
                           ],
                           "timestamp" : 1759706216,
                           "is_valid" : true,
                           "power" : 0,
                           "total" : 472
                        },
                        {
                           "overpower" : 0,
                           "counters" : [
                              0,
                              0,
                              0
                           ],
                           "timestamp" : 1759706216,
                           "power" : 0,
                           "is_valid" : true,
                           "total" : 438
                        }
                     ],
                     "ram_total" : 50720,
                     "rollers" : [
                        {
                           "power" : 0,
                           "positioning" : true,
                           "current_pos" : ${current_pos},
                           "safety_switch" : false,
                           "source" : "cloud",
                           "last_direction" : "close",
                           "is_valid" : true,
                           "calibrating" : false,
                           "stop_reason" : "normal",
                           "overtemperature" : false,
                           "state" : "${state}"
                        }
                     ],
                     "fs_free" : 145329,
                     "temperature_status" : "Normal",
                     "tmp" : {
                        "tF" : 111.69,
                        "is_valid" : true,
                        "tC" : 44.27
                     },
                     "voltage" : 219.87
                  }
               }
            ]""";

    private static HttpResponse statusResponse(String state, int current_pos) {
        var substitor = new StringSubstitutor(Map.of("state", state, "current_pos", current_pos));
        return response(substitor.replace(STATUS_TEMPLATE));
    }

    private static HttpResponse response(String body) {
        return new HttpResponse(200, "http://example.org/", false, new ByteArrayInputStream(body.getBytes()));
    }
}
