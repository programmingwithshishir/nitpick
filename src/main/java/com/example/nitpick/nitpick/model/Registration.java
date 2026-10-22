package com.example.nitpick.nitpick.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "registrations", uniqueConstraints = {
    @UniqueConstraint(name = "unique_user_event_slot", columnNames = {"event_id", "user_id", "slot_number"})
})
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "slot_number")
    private Integer slotNumber;

    @Column(name = "registered_at", updatable = false)
    private LocalDateTime registeredAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private RegistrationStatus status = RegistrationStatus.CONFIRMED;

    public Registration() {
        super();
        this.status = RegistrationStatus.CONFIRMED;
    }

    public Registration(Long id, Event event, User user, Integer slotNumber,
                        LocalDateTime registeredAt, RegistrationStatus status) {
        super();
        this.id = id;
        this.event = event;
        this.user = user;
        this.slotNumber = slotNumber;
        this.registeredAt = registeredAt;
        this.status = status != null ? status : RegistrationStatus.CONFIRMED;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Event event;
        private User user;
        private Integer slotNumber;
        private LocalDateTime registeredAt;
        private RegistrationStatus status = RegistrationStatus.CONFIRMED;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder event(Event event) { this.event = event; return this; }
        public Builder user(User user) { this.user = user; return this; }
        public Builder slotNumber(Integer slotNumber) { this.slotNumber = slotNumber; return this; }
        public Builder registeredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; return this; }
        public Builder status(RegistrationStatus status) { this.status = status; return this; }

        public Registration build() {
            return new Registration(id, event, user, slotNumber, registeredAt, status);
        }
    }

    @PrePersist
    protected void onCreate() {
        this.registeredAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = RegistrationStatus.CONFIRMED;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Integer getSlotNumber() { return slotNumber; }
    public void setSlotNumber(Integer slotNumber) { this.slotNumber = slotNumber; }

    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }

    public RegistrationStatus getStatus() { return status; }
    public void setStatus(RegistrationStatus status) { this.status = status; }
}
