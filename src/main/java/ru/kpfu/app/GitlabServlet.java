package ru.kpfu.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import ru.kpfu.models.webhooks.PiAction;
import ru.kpfu.models.webhooks.issue.IssueHook;
import ru.kpfu.models.webhooks.mr.MergeRequestHook;
import ru.kpfu.models.webhooks.pipeline.PipelineHook;
import ru.kpfu.models.webhooks.push.PushHook;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

/**
 * Принимает запросы вебхуков Gitlab, преобразует в сообщения для отправки в MQTT брокер
 */
@WebServlet("")
public class GitlabServlet extends HttpServlet {
    private static final String GITLAB_EVENT_HEADER = "X-Gitlab-Event";
    private static final String GITLAB_PUSH_EVENT = "Push Hook";
    private static final String GITLAB_ISSUE_EVENT = "Issue Hook";
    private static final String GITLAB_MR_EVENT = "Merge Request Hook";
    private static final String GITLAB_PIPELINE_EVENT = "Pipeline Hook";

    private final ObjectMapper jsonMapper;
    private final MqttPublisher mqttPublisher;

    public GitlabServlet() {
        this.jsonMapper = new JsonMapper();
        this.mqttPublisher = new MqttPublisher();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String event = req.getHeader(GITLAB_EVENT_HEADER);
        if (event == null || event.isEmpty()) {
            return;
        }
        InputStream is = req.getInputStream();
        switch (event) {
            case GITLAB_PUSH_EVENT:
                processPushEvent();
                return;
            case GITLAB_ISSUE_EVENT:
                processIssueEvent(is);
                return;
            case GITLAB_MR_EVENT:
                processMREvent(is);
                return;
            case GITLAB_PIPELINE_EVENT:
                processPipelineEvent(is);
                return;
        }
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    private void processPushEvent() {
        PiAction action = PushHook.ACTION;
        if (action == PiAction.NONE) {
            return;
        }
        mqttPublisher.sendMessage(PushHook.ACTION.getPiActionValue());
    }

    private void processMREvent(InputStream inputStream) throws IOException {
        if (MergeRequestHook.ACTION == PiAction.NONE) {
            return;
        }
        MergeRequestHook hook = jsonMapper.readValue(inputStream, MergeRequestHook.class);
        if (hook == null || hook.getObjectAttributes() == null ||
                !"opened".equals(hook.getObjectAttributes().getState())) {
            return;
        }
        mqttPublisher.sendMessage(MergeRequestHook.ACTION.getPiActionValue());
    }

    private void processIssueEvent(InputStream inputStream) throws IOException {
        if (IssueHook.ACTION == PiAction.NONE) {
            return;
        }
        IssueHook hook = jsonMapper.readValue(inputStream, IssueHook.class);
        if (hook == null || hook.getObjectAttributes() == null ||
                !"opened".equals(hook.getObjectAttributes().getState())) {
            return;
        }
        mqttPublisher.sendMessage(IssueHook.ACTION.getPiActionValue());
    }

    private void processPipelineEvent(InputStream inputStream) throws IOException {
        if (PipelineHook.ACTION == PiAction.NONE) {
            return;
        }
        PipelineHook hook = jsonMapper.readValue(inputStream, PipelineHook.class);
        if (hook == null || hook.getObjectAttributes() == null ||
                !"failed".equals(hook.getObjectAttributes().getStatus())) {
            return;
        }
        mqttPublisher.sendMessage(PipelineHook.ACTION.getPiActionValue());
    }
}
