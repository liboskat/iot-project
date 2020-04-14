package ru.kpfu.models.webhooks.pipeline;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import ru.kpfu.models.webhooks.PiAction;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PipelineHook {
    public static final PiAction ACTION = PiAction.SOUND_PIPELINE;

    private PipelineObjectAttributes objectAttributes;

    public PipelineObjectAttributes getObjectAttributes() {
        return objectAttributes;
    }

    @JsonSetter("object_attributes")
    public void setObjectAttributes(PipelineObjectAttributes objectAttributes) {
        this.objectAttributes = objectAttributes;
    }
}
