package ru.kpfu.app;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ru.kpfu.models.api.Pipeline;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Слушает сообщения из топика с командами, перезапускает последний неудавшийся пайплайн (если такие есть за 2 часа)
 */
public class PiCommandListener {
    private static final String GITLAB_PROJECT_ID = "12345678";
    private static final String GITLAB_API_URL = "https://gitlab.com/api/v4/projects/";
    private static final String GITLAB_TOKEN_HEADER_NAME = "Private-Token";
    private static final String GITLAB_TOKEN_HEADER_VALUE = "SUPER SECRET TOKEN";
    private static final long GITLAB_PIPELINE_TIME_PERIOD_IN_HOURS = 2;
    private static final String PI_REBUILD_COMMAND = "rebuild";
    private static final String PI_COMMANDS_CHANNEL = "there is pi commands channel";
    private final ObjectMapper jsonMapper;
    private final IMqttClient listener;

    public PiCommandListener() {
        jsonMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
        try {
            listener = new MqttClient(Main.MQTT_BROKER_ADDRESS, UUID.randomUUID().toString());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            listener.connect(options);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    public void startListening() {
        try {
            listener.subscribe(PI_COMMANDS_CHANNEL, (topic, message) -> {
                byte[] payload = message.getPayload();
                if (payload.length == 0) {
                    return;
                }
                String command = new String(payload, StandardCharsets.UTF_8);
                if (PI_REBUILD_COMMAND.equals(command.toLowerCase())) {
                    restartLastPipeline();
                }
            });
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    private void restartLastPipeline() {
        loadPipelines().stream()
                .max(Comparator.comparing(Pipeline::getUpdatedAt))
                .ifPresent(p -> restartPipeline(p.getId()));
    }

    private List<Pipeline> loadPipelines() {
        String url = GITLAB_API_URL + GITLAB_PROJECT_ID + "/pipelines" +
                "?status=failed" +
                "&updated_after=" + Instant.now().minus(GITLAB_PIPELINE_TIME_PERIOD_IN_HOURS, ChronoUnit.HOURS);
        HttpURLConnection con;
        List<Pipeline> pipelines = Collections.emptyList();
        try {
            con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestProperty(GITLAB_TOKEN_HEADER_NAME, GITLAB_TOKEN_HEADER_VALUE);
            con.setRequestMethod("GET");
            if (con.getResponseCode() >= 300) {
                InputStream is = con.getErrorStream();
                System.out.println(new BufferedReader(new InputStreamReader(is)).lines()
                        .collect(Collectors.joining("\n")));
                is.close();
                con.disconnect();
                return pipelines;
            }
            pipelines = jsonMapper.readValue(con.getInputStream(), new TypeReference<List<Pipeline>>() {});
            con.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pipelines;
    }

    private void restartPipeline(String id) {
        String url = GITLAB_API_URL + GITLAB_PROJECT_ID + "/pipelines/" + id + "/retry";
        HttpURLConnection con;
        try {
            con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestProperty(GITLAB_TOKEN_HEADER_NAME, GITLAB_TOKEN_HEADER_VALUE);
            con.setRequestMethod("POST");
            InputStream is;
            if (con.getResponseCode() >= 300) {
                is = con.getErrorStream();
            } else {
                is = con.getInputStream();
            }
            System.out.println(new BufferedReader(new InputStreamReader(is)).lines()
                    .collect(Collectors.joining("\n")));
            is.close();
            con.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
