package ru.kpfu.models.webhooks.mr;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import ru.kpfu.models.webhooks.PiAction;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MergeRequestHook {
    public static final PiAction ACTION = PiAction.SOUND_MR;

    private MergeRequestObjectAttributes objectAttributes;

    public MergeRequestObjectAttributes getObjectAttributes() {
        return objectAttributes;
    }

    @JsonSetter("object_attributes")
    public void setObjectAttributes(MergeRequestObjectAttributes objectAttributes) {
        this.objectAttributes = objectAttributes;
    }
}
