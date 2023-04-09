package de.malkusch.ha.automation.infrastructure.socket;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.smarthomej.binding.tuya.internal.local.DeviceInfoSubscriber;
import org.smarthomej.binding.tuya.internal.local.TuyaDevice;
import org.smarthomej.binding.tuya.internal.local.UdpDiscoveryListener;
import org.smarthomej.binding.tuya.internal.local.dto.DeviceInfo;

import com.google.gson.Gson;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class TuyaSocketFactory implements AutoCloseable {

    private final Gson gson;
    private final EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
    private final Duration timeout;
    private final Duration expiration;

    private class Discovery implements DeviceInfoSubscriber {

        private volatile DeviceInfo deviceInfo;
        private final CountDownLatch latch = new CountDownLatch(1);

        @Override
        public void deviceInfoChanged(DeviceInfo deviceInfo) {
            this.deviceInfo = deviceInfo;
            log.debug("Discovered {}", deviceInfo);
            latch.countDown();
        }

        public DeviceInfo discover(String deviceId) throws IOException {
            var listener = new UdpDiscoveryListener(eventLoopGroup);
            try {
                listener.registerListener(deviceId, this);
                try {
                    latch.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Discovery was interrupted", e);
                }
                if (deviceInfo == null) {
                    throw new IOException("Discovery timed out");
                }
                return deviceInfo;

            } finally {
                listener.unregisterListener(this);
                listener.deactivate();
            }
        }
    }

    public Socket build(String deviceId, String localKey) throws IOException {
        var discovery = new Discovery();
        var deviceInfo = discovery.discover(deviceId);

        var stateUpdater = new TuyaState.StateUpdater();
        var reconnectUpdated = new ReconnectingTuyaSocket.ReconnectListener(stateUpdater);

        var tuyaDevice = new TuyaDevice(gson, reconnectUpdated, eventLoopGroup, deviceId,
                localKey.getBytes(StandardCharsets.UTF_8), deviceInfo.ip, deviceInfo.protocolVersion);

        var state = new TuyaState(tuyaDevice, stateUpdater, timeout, expiration);
        var socket = new TuyaSocket(tuyaDevice, state);

        var reconnecting = new ReconnectingTuyaSocket(socket, tuyaDevice, timeout, reconnectUpdated);

        return reconnecting;
    }

    @Override
    public void close() throws Exception {
        eventLoopGroup.shutdownGracefully().await(timeout.toMillis());
    }
}
