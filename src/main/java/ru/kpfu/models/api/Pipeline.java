package ru.kpfu.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.time.ZonedDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Pipeline {
    private String id;
    private String status;
    private ZonedDateTime updatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    @JsonSetter("updated_at")
    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
