package de.malkusch.ha.monitoring.infrastructure;

import static com.hivemq.client.mqtt.MqttGlobalPublishFilter.ALL;
import static java.nio.charset.StandardCharsets.UTF_8;
import static lombok.AccessLevel.PRIVATE;

import java.util.Collection;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;

import io.prometheus.client.Gauge;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor(access = PRIVATE)
@Slf4j
public class MqttMonitoring<MESSAGE> {

    @RequiredArgsConstructor
    @Component
    public static class Factory {

        private final ObjectMapper mapper;
        private final Mqtt5BlockingClient mqtt;

        public <MESSAGE> MqttMonitoring<MESSAGE> build(Class<MESSAGE> type, String topic,
                Collection<MessageGauge<MESSAGE>> fieldPollers) {
            var poller = new MqttMonitoring<>(fieldPollers);
            mqtt.subscribeWith().topicFilter(topic).send();

            mqtt.toAsync().publishes(ALL, publish -> {
                var jsonMessage = UTF_8.decode(publish.getPayload().get()).toString();
                try {
                    var message = mapper.readValue(jsonMessage, type);
                    poller.update(message);
                } catch (Exception e) {
                    log.error("Updating MqttMonitoring failed for topic {} with message {}", topic, jsonMessage, e);
                }
            });

            return poller;
        }
    }

    @RequiredArgsConstructor
    static final class MessageGauge<MESSAGE> {

        public static <MESSAGE> MessageGauge<MESSAGE> messageGauge(String name,
                Function<MESSAGE, Double> fieldMapper) {
            var help = name;
            var gauge = Gauge.build().name(name).help(help).create();
            gauge.register();

            return new MessageGauge<>(gauge, fieldMapper);
        }

        private final Gauge gauge;

        private final Function<MESSAGE, Double> fieldMapper;

        void update(MESSAGE message) {
            var value = fieldMapper.apply(message);
            gauge.set(value);
        }
    }

    private final Collection<MessageGauge<MESSAGE>> messageGauges;

    public void update(MESSAGE message) {
        messageGauges.forEach(it -> it.update(message));
    }
}
