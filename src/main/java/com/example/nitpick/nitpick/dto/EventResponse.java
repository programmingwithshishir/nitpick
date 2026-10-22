package com.example.nitpick.nitpick.dto;

import com.example.nitpick.nitpick.model.EventStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class EventResponse {
    private Long id;
    private Long venueId;
    private String venueName;
    private String name;
    private String description;
    private LocalDate eventDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer totalSlots;
    private Integer bookedSlots;
    private Integer availableSlots;
    private EventStatus status;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public EventResponse() {}

    public EventResponse(Long id, Long venueId, String venueName, String name, String description,
                         LocalDate eventDate, LocalTime startTime, LocalTime endTime, Integer totalSlots,
                         Integer bookedSlots, Integer availableSlots, EventStatus status, Integer version,
                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.venueId = venueId;
        this.venueName = venueName;
        this.name = name;
        this.description = description;
        this.eventDate = eventDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalSlots = totalSlots;
        this.bookedSlots = bookedSlots;
        this.availableSlots = availableSlots;
        this.status = status;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long venueId;
        private String venueName;
        private String name;
        private String description;
        private LocalDate eventDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private Integer totalSlots;
        private Integer bookedSlots;
        private Integer availableSlots;
        private EventStatus status;
        private Integer version;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder venueId(Long venueId) { this.venueId = venueId; return this; }
        public Builder venueName(String venueName) { this.venueName = venueName; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder eventDate(LocalDate eventDate) { this.eventDate = eventDate; return this; }
        public Builder startTime(LocalTime startTime) { this.startTime = startTime; return this; }
        public Builder endTime(LocalTime endTime) { this.endTime = endTime; return this; }
        public Builder totalSlots(Integer totalSlots) { this.totalSlots = totalSlots; return this; }
        public Builder bookedSlots(Integer bookedSlots) { this.bookedSlots = bookedSlots; return this; }
        public Builder availableSlots(Integer availableSlots) { this.availableSlots = availableSlots; return this; }
        public Builder status(EventStatus status) { this.status = status; return this; }
        public Builder version(Integer version) { this.version = version; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public EventResponse build() {
            return new EventResponse(id, venueId, venueName, name, description, eventDate, startTime,
                    endTime, totalSlots, bookedSlots, availableSlots, status, version, createdAt, updatedAt);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVenueId() { return venueId; }
    public void setVenueId(Long venueId) { this.venueId = venueId; }

    public String getVenueName() { return venueName; }
    public void setVenueName(String venueName) { this.venueName = venueName; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public Integer getTotalSlots() { return totalSlots; }
    public void setTotalSlots(Integer totalSlots) { this.totalSlots = totalSlots; }

    public Integer getBookedSlots() { return bookedSlots; }
    public void setBookedSlots(Integer bookedSlots) { this.bookedSlots = bookedSlots; }

    public Integer getAvailableSlots() { return availableSlots; }
    public void setAvailableSlots(Integer availableSlots) { this.availableSlots = availableSlots; }

    public EventStatus getStatus() { return status; }
    public void setStatus(EventStatus status) { this.status = status; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
