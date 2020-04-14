package ru.kpfu.app;

import org.eclipse.paho.client.mqttv3.*;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Публикует сообщения в топик MQTT брокера
 */
public class MqttPublisher {
    private static final ExecutorService THREAD_POOL = Executors.newSingleThreadExecutor();
    private static final String GITLAB_EVENTS_CHANNEL = "there is gitlab events channel";

    private final IMqttClient publisher;

    public MqttPublisher() {
        try {
            this.publisher = new MqttClient(Main.MQTT_BROKER_ADDRESS, UUID.randomUUID().toString());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            publisher.connect(options);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(String message) {
        THREAD_POOL.submit(new SendMessageRunnable(message));
    }

    private class SendMessageRunnable implements Runnable {
        private byte[] payload;

        SendMessageRunnable(String message) {
            this.payload = message.getBytes();
        }

        @Override
        public void run() {
            if (!publisher.isConnected()) {
                return;
            }
            MqttMessage msg = new MqttMessage(payload);
            msg.setRetained(false);
            msg.setQos(0);
            try {
                publisher.publish(GITLAB_EVENTS_CHANNEL, msg);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }
}
