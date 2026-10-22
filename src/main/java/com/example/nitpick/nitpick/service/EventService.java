package com.example.nitpick.nitpick.service;

import com.example.nitpick.nitpick.dto.EventRequest;
import com.example.nitpick.nitpick.dto.EventResponse;
import com.example.nitpick.nitpick.exception.BadRequestException;
import com.example.nitpick.nitpick.exception.ConflictException;
import com.example.nitpick.nitpick.exception.ResourceNotFoundException;
import com.example.nitpick.nitpick.model.Event;
import com.example.nitpick.nitpick.model.EventStatus;
import com.example.nitpick.nitpick.model.Venue;
import com.example.nitpick.nitpick.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final VenueService venueService;

    @Transactional
    public EventResponse createEvent(EventRequest request) {
        validateEventTimes(request);

        Venue venue = venueService.getVenueEntity(request.getVenueId());
        if (!Boolean.TRUE.equals(venue.getIsActive())) {
            throw new BadRequestException("Cannot create event for an inactive venue");
        }

        if (request.getTotalSlots() > venue.getCapacity()) {
            throw new BadRequestException(
                    String.format("Total slots (%d) cannot exceed venue capacity (%d)",
                            request.getTotalSlots(), venue.getCapacity()));
        }

        checkVenueAvailability(venue.getId(), request, null);

        Event event = Event.builder()
                .venue(venue)
                .name(request.getName().trim())
                .description(request.getDescription())
                .eventDate(request.getEventDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .totalSlots(request.getTotalSlots())
                .bookedSlots(0)
                .status(request.getStatus() != null ? request.getStatus() : EventStatus.ACTIVE)
                .build();

        Event saved = eventRepository.save(event);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public EventResponse getEventById(Long id) {
        Event event = getEventEntity(id);
        return mapToResponse(event);
    }

    @Transactional(readOnly = true)
    public Event getEventEntity(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getEvents(Long venueId, LocalDate date, EventStatus status) {
        List<Event> events;

        if (venueId != null && date != null) {
            events = eventRepository.findByVenueIdAndEventDate(venueId, date);
        } else if (venueId != null) {
            events = eventRepository.findByVenueId(venueId);
        } else if (date != null) {
            events = eventRepository.findByEventDate(date);
        } else if (status != null) {
            events = eventRepository.findByStatus(status);
        } else {
            events = eventRepository.findAll();
        }

        if (status != null && (venueId != null || date != null)) {
            events = events.stream().filter(e -> e.getStatus() == status).toList();
        }

        return events.stream().map(this::mapToResponse).toList();
    }

    @Transactional
    public EventResponse updateEvent(Long id, EventRequest request) {
        Event event = getEventEntity(id);

        validateEventTimes(request);

        Venue venue = venueService.getVenueEntity(request.getVenueId());
        if (!Boolean.TRUE.equals(venue.getIsActive())) {
            throw new BadRequestException("Cannot assign event to an inactive venue");
        }

        if (request.getTotalSlots() > venue.getCapacity()) {
            throw new BadRequestException(
                    String.format("Total slots (%d) cannot exceed venue capacity (%d)",
                            request.getTotalSlots(), venue.getCapacity()));
        }

        if (request.getTotalSlots() < event.getBookedSlots()) {
            throw new BadRequestException(
                    String.format("Cannot reduce total slots (%d) below current booked slots (%d)",
                            request.getTotalSlots(), event.getBookedSlots()));
        }

        checkVenueAvailability(venue.getId(), request, id);

        event.setVenue(venue);
        event.setName(request.getName().trim());
        event.setDescription(request.getDescription());
        event.setEventDate(request.getEventDate());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setTotalSlots(request.getTotalSlots());
        if (request.getStatus() != null) {
            event.setStatus(request.getStatus());
        }

        Event updated = eventRepository.save(event);
        return mapToResponse(updated);
    }

    @Transactional
    public EventResponse cancelEvent(Long id) {
        Event event = getEventEntity(id);
        event.setStatus(EventStatus.CANCELLED);
        Event updated = eventRepository.save(event);
        return mapToResponse(updated);
    }

    private void validateEventTimes(EventRequest request) {
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new BadRequestException("Event start time must be before end time");
        }
    }

    private void checkVenueAvailability(Long venueId, EventRequest request, Long excludeEventId) {
        List<Event> conflicts = eventRepository.findConflictingEvents(
                venueId,
                request.getEventDate(),
                request.getStartTime(),
                request.getEndTime(),
                EventStatus.ACTIVE,
                excludeEventId
        );

        if (!conflicts.isEmpty()) {
            Event conflict = conflicts.get(0);
            throw new ConflictException(String.format(
                    "Venue is already booked for event '%s' on %s between %s and %s",
                    conflict.getName(),
                    conflict.getEventDate(),
                    conflict.getStartTime(),
                    conflict.getEndTime()
            ));
        }
    }

    public EventResponse mapToResponse(Event event) {
        int availableSlots = Math.max(0, event.getTotalSlots() - (event.getBookedSlots() != null ? event.getBookedSlots() : 0));
        return EventResponse.builder()
                .id(event.getId())
                .venueId(event.getVenue().getId())
                .venueName(event.getVenue().getName())
                .name(event.getName())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .totalSlots(event.getTotalSlots())
                .bookedSlots(event.getBookedSlots() != null ? event.getBookedSlots() : 0)
                .availableSlots(availableSlots)
                .status(event.getStatus())
                .version(event.getVersion())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }
}
