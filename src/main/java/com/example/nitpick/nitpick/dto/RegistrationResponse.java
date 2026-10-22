package com.example.nitpick.nitpick.dto;

import com.example.nitpick.nitpick.model.RegistrationStatus;
import java.time.LocalDateTime;

public class RegistrationResponse {
    private Long id;
    private Long eventId;
    private String eventName;
    private Long userId;
    private String userName;
    private String userEmail;
    private Integer slotNumber;
    private LocalDateTime registeredAt;
    private RegistrationStatus status;

    public RegistrationResponse() {}

    public RegistrationResponse(Long id, Long eventId, String eventName, Long userId, String userName,
                                String userEmail, Integer slotNumber, LocalDateTime registeredAt,
                                RegistrationStatus status) {
        this.id = id;
        this.eventId = eventId;
        this.eventName = eventName;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.slotNumber = slotNumber;
        this.registeredAt = registeredAt;
        this.status = status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long eventId;
        private String eventName;
        private Long userId;
        private String userName;
        private String userEmail;
        private Integer slotNumber;
        private LocalDateTime registeredAt;
        private RegistrationStatus status;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder eventId(Long eventId) { this.eventId = eventId; return this; }
        public Builder eventName(String eventName) { this.eventName = eventName; return this; }
        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder userName(String userName) { this.userName = userName; return this; }
        public Builder userEmail(String userEmail) { this.userEmail = userEmail; return this; }
        public Builder slotNumber(Integer slotNumber) { this.slotNumber = slotNumber; return this; }
        public Builder registeredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; return this; }
        public Builder status(RegistrationStatus status) { this.status = status; return this; }

        public RegistrationResponse build() {
            return new RegistrationResponse(id, eventId, eventName, userId, userName, userEmail,
                    slotNumber, registeredAt, status);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public Integer getSlotNumber() { return slotNumber; }
    public void setSlotNumber(Integer slotNumber) { this.slotNumber = slotNumber; }

    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }

    public RegistrationStatus getStatus() { return status; }
    public void setStatus(RegistrationStatus status) { this.status = status; }
}
