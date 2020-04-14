package ru.kpfu.models.webhooks;

public enum PiAction {
    SOUND_ISSUE("issue"),
    SOUND_MR("mr"),
    SOUND_PIPELINE("pipeline"),
    SOUND_PUSH("push"),
    NONE("none");

    private final String piActionValue;

    PiAction(String piActionValue) {
        this.piActionValue = piActionValue;
    }

    public String getPiActionValue() {
        return piActionValue;
    }
}
