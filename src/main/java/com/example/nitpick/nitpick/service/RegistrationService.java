package com.example.nitpick.nitpick.service;

import com.example.nitpick.nitpick.dto.RegistrationRequest;
import com.example.nitpick.nitpick.dto.RegistrationResponse;
import com.example.nitpick.nitpick.exception.BadRequestException;
import com.example.nitpick.nitpick.exception.ConflictException;
import com.example.nitpick.nitpick.exception.ResourceNotFoundException;
import com.example.nitpick.nitpick.model.*;
import com.example.nitpick.nitpick.repository.EventRepository;
import com.example.nitpick.nitpick.repository.RegistrationRepository;
import com.example.nitpick.nitpick.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public RegistrationService(RegistrationRepository registrationRepository,
                               EventRepository eventRepository,
                               UserRepository userRepository) {
        this.registrationRepository = registrationRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public RegistrationResponse registerForEvent(RegistrationRequest request) {
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + request.getEventId()));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new BadRequestException("Inactive user cannot register for events");
        }

        if (event.getStatus() != EventStatus.ACTIVE) {
            throw new BadRequestException("Cannot register for event with status: " + event.getStatus());
        }

        boolean alreadyRegistered = registrationRepository.existsByEventIdAndUserIdAndStatus(
                event.getId(), user.getId(), RegistrationStatus.CONFIRMED);
        if (alreadyRegistered) {
            throw new ConflictException("User is already registered for this event");
        }

        int currentBooked = event.getBookedSlots() != null ? event.getBookedSlots() : 0;
        if (currentBooked >= event.getTotalSlots()) {
            throw new ConflictException("Event is fully booked. No slots available.");
        }

        Set<Integer> bookedSlots = registrationRepository.findBookedSlotNumbers(
                event.getId(), RegistrationStatus.CANCELLED);

        Integer allocatedSlot = request.getSlotNumber();
        if (allocatedSlot != null) {
            if (allocatedSlot < 1 || allocatedSlot > event.getTotalSlots()) {
                throw new BadRequestException(
                        String.format("Slot number must be between 1 and %d", event.getTotalSlots()));
            }
            if (bookedSlots.contains(allocatedSlot)) {
                throw new ConflictException("Slot number " + allocatedSlot + " is already reserved");
            }
        } else {
            // Auto-assign the lowest available slot number
            for (int i = 1; i <= event.getTotalSlots(); i++) {
                if (!bookedSlots.contains(i)) {
                    allocatedSlot = i;
                    break;
                }
            }
            if (allocatedSlot == null) {
                throw new ConflictException("No available slot could be allocated");
            }
        }

        event.setBookedSlots(currentBooked + 1);
        eventRepository.save(event);

        Registration registration = Registration.builder()
                .event(event)
                .user(user)
                .slotNumber(allocatedSlot)
                .status(RegistrationStatus.CONFIRMED)
                .build();

        Registration saved = registrationRepository.save(registration);
        return mapToResponse(saved);
    }

    @Transactional
    public RegistrationResponse cancelRegistration(Long id) {
        Registration registration = registrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found with id: " + id));

        if (registration.getStatus() == RegistrationStatus.CANCELLED) {
            throw new BadRequestException("Registration is already cancelled");
        }

        registration.setStatus(RegistrationStatus.CANCELLED);
        Registration updated = registrationRepository.save(registration);

        Event event = registration.getEvent();
        int currentBooked = event.getBookedSlots() != null ? event.getBookedSlots() : 0;
        if (currentBooked > 0) {
            event.setBookedSlots(currentBooked - 1);
            eventRepository.save(event);
        }

        return mapToResponse(updated);
    }

    @Transactional(readOnly = true)
    public RegistrationResponse getRegistrationById(Long id) {
        Registration registration = registrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found with id: " + id));
        return mapToResponse(registration);
    }

    @Transactional(readOnly = true)
    public List<RegistrationResponse> getRegistrationsByEvent(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event not found with id: " + eventId);
        }
        return registrationRepository.findByEventId(eventId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RegistrationResponse> getRegistrationsByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        return registrationRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public RegistrationResponse mapToResponse(Registration registration) {
        User user = registration.getUser();
        Event event = registration.getEvent();
        return RegistrationResponse.builder()
                .id(registration.getId())
                .eventId(event.getId())
                .eventName(event.getName())
                .userId(user.getId())
                .userName(user.getFirstName() + " " + user.getLastName())
                .userEmail(user.getEmail())
                .slotNumber(registration.getSlotNumber())
                .registeredAt(registration.getRegisteredAt())
                .status(registration.getStatus())
                .build();
    }
}
