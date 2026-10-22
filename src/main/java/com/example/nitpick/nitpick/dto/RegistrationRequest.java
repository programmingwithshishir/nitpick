package com.example.nitpick.nitpick.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class RegistrationRequest {

    @NotNull(message = "Event ID is required")
    private Long eventId;

    @NotNull(message = "User ID is required")
    private Long userId;

    @Min(value = 1, message = "Slot number must be at least 1")
    private Integer slotNumber;

    public RegistrationRequest() {}

    public RegistrationRequest(Long eventId, Long userId, Integer slotNumber) {
        this.eventId = eventId;
        this.userId = userId;
        this.slotNumber = slotNumber;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long eventId;
        private Long userId;
        private Integer slotNumber;

        public Builder eventId(Long eventId) { this.eventId = eventId; return this; }
        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder slotNumber(Integer slotNumber) { this.slotNumber = slotNumber; return this; }

        public RegistrationRequest build() {
            return new RegistrationRequest(eventId, userId, slotNumber);
        }
    }

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Integer getSlotNumber() { return slotNumber; }
    public void setSlotNumber(Integer slotNumber) { this.slotNumber = slotNumber; }
}
