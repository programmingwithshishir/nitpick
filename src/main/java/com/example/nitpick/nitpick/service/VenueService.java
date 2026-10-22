package com.example.nitpick.nitpick.service;

import com.example.nitpick.nitpick.dto.VenueRequest;
import com.example.nitpick.nitpick.dto.VenueResponse;
import com.example.nitpick.nitpick.exception.ResourceNotFoundException;
import com.example.nitpick.nitpick.model.Venue;
import com.example.nitpick.nitpick.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VenueService {

    private final VenueRepository venueRepository;

    @Transactional
    public VenueResponse createVenue(VenueRequest request) {
        Venue venue = Venue.builder()
                .name(request.getName().trim())
                .capacity(request.getCapacity())
                .location(request.getLocation() != null ? request.getLocation().trim() : null)
                .description(request.getDescription())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        Venue savedVenue = venueRepository.save(venue);
        return mapToResponse(savedVenue);
    }

    @Transactional(readOnly = true)
    public VenueResponse getVenueById(Long id) {
        Venue venue = getVenueEntity(id);
        return mapToResponse(venue);
    }

    @Transactional(readOnly = true)
    public Venue getVenueEntity(Long id) {
        return venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<VenueResponse> getAllVenues(Boolean activeOnly) {
        List<Venue> venues;
        if (Boolean.TRUE.equals(activeOnly)) {
            venues = venueRepository.findByIsActive(true);
        } else {
            venues = venueRepository.findAll();
        }
        return venues.stream().map(this::mapToResponse).toList();
    }

    @Transactional
    public VenueResponse updateVenue(Long id, VenueRequest request) {
        Venue venue = getVenueEntity(id);

        venue.setName(request.getName().trim());
        venue.setCapacity(request.getCapacity());
        venue.setLocation(request.getLocation() != null ? request.getLocation().trim() : null);
        venue.setDescription(request.getDescription());
        if (request.getIsActive() != null) {
            venue.setIsActive(request.getIsActive());
        }

        Venue updatedVenue = venueRepository.save(venue);
        return mapToResponse(updatedVenue);
    }

    @Transactional
    public void deactivateVenue(Long id) {
        Venue venue = getVenueEntity(id);
        venue.setIsActive(false);
        venueRepository.save(venue);
    }

    public VenueResponse mapToResponse(Venue venue) {
        return VenueResponse.builder()
                .id(venue.getId())
                .name(venue.getName())
                .capacity(venue.getCapacity())
                .location(venue.getLocation())
                .description(venue.getDescription())
                .isActive(venue.getIsActive())
                .createdAt(venue.getCreatedAt())
                .updatedAt(venue.getUpdatedAt())
                .build();
    }
}
