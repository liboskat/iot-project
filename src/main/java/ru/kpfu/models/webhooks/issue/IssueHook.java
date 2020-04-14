package ru.kpfu.models.webhooks.issue;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import ru.kpfu.models.webhooks.PiAction;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IssueHook {
    public static final PiAction ACTION = PiAction.SOUND_ISSUE;

    private IssueObjectAttributes objectAttributes;

    public IssueObjectAttributes getObjectAttributes() {
        return objectAttributes;
    }

    @JsonSetter("object_attributes")
    public void setObjectAttributes(IssueObjectAttributes objectAttributes) {
        this.objectAttributes = objectAttributes;
    }
}
