package com.example.nitpick.nitpick.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class VenueRequest {

    @NotBlank(message = "Venue name is required")
    @Size(max = 200, message = "Venue name must not exceed 200 characters")
    private String name;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    @Size(max = 500, message = "Location must not exceed 500 characters")
    private String location;

    private String description;

    private Boolean isActive = true;

    public VenueRequest() {
        this.isActive = true;
    }

    public VenueRequest(String name, Integer capacity, String location, String description, Boolean isActive) {
        this.name = name;
        this.capacity = capacity;
        this.location = location;
        this.description = description;
        this.isActive = isActive != null ? isActive : true;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private Integer capacity;
        private String location;
        private String description;
        private Boolean isActive = true;

        public Builder name(String name) { this.name = name; return this; }
        public Builder capacity(Integer capacity) { this.capacity = capacity; return this; }
        public Builder location(String location) { this.location = location; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder isActive(Boolean isActive) { this.isActive = isActive; return this; }

        public VenueRequest build() {
            return new VenueRequest(name, capacity, location, description, isActive);
        }
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
