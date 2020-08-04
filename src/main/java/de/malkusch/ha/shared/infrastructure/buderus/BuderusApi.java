package de.malkusch.ha.shared.infrastructure.buderus;

import java.math.BigDecimal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.malkusch.ha.shared.model.ApiException;
import lombok.Value;

// See https://github.com/hlipka/buderus2mqtt
// See https://github.com/openhab/openhab1-addons/tree/master/bundles/binding/org.openhab.binding.km200
public final class BuderusApi {

    private final KM200Device device;
    private final KM200Comm comm;
    private final ObjectMapper mapper;

    BuderusApi(String host, String gatewayPassword, String privatePassword, String salt, ObjectMapper mapper) {
        var device = new KM200Device();
        // device.setCharSet("UTF-8");
        device.setGatewayPassword(gatewayPassword.replace("-", ""));
        device.setPrivatePassword(privatePassword);
        device.setIP4Address(host);
        device.setMD5Salt(salt);
        device.setInited(true);
        this.device = device;

        var comm = new KM200Comm();
        comm.getDataFromService(device, "/system");
        this.comm = comm;

        this.mapper = mapper;
    }

    public void listURIs() {
        comm.initObjects(device, "/system");
        comm.initObjects(device, "/dhwCircuits");
        comm.initObjects(device, "/gateway");
        comm.initObjects(device, "/heatingCircuits");
        comm.initObjects(device, "/heatSources");
        comm.initObjects(device, "/notifications");
        comm.initObjects(device, "/recordings");
        comm.initObjects(device, "/solarCircuits");

        device.listAllServices();
    }

    @Value
    private static class UpdateString {
        public final String value;
    }

    public void update(String path, String value) throws ApiException {
        var update = new UpdateString(value);
        update(path, update);
    }

    @Value
    private static class UpdateFloat {
        public final BigDecimal value;
    }

    public void update(String path, int value) throws ApiException {
        update(path, new BigDecimal(value));
    }

    public void update(String path, BigDecimal value) throws ApiException {
        var update = new UpdateFloat(value);
        update(path, update);
    }

    private void update(String path, Object update) throws ApiException {
        String json = null;
        try {
            json = mapper.writeValueAsString(update);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to update " + path, e);
        }
        var encrypted = comm.encodeMessage(device, json);
        if (encrypted == null) {
            throw new ApiException("Could not encrypt update " + json);
        }
        var response = comm.sendDataToService(device, path, encrypted);
        if (!(response >= 200 && response < 300)) {
            throw new ApiException(String.format("Failed to update %s [%d]", path, response));
        }
    }

    public JsonNode query(String path) throws ApiException {
        var encrypted = comm.getDataFromService(device, path);
        if (encrypted == null) {
            throw new ApiException("No response when querying " + path);
        }
        var decrypted = comm.decodeMessage(device, encrypted);
        if (decrypted == null) {
            throw new ApiException("Could not decrypt query " + path);
        }

        try {
            return mapper.readTree(decrypted);

        } catch (JsonProcessingException e) {
            throw new ApiException("Failed to query " + path + ": " + decrypted, e);
        }
    }
}
