package com.example.nitpick.nitpick.controller;

import com.example.nitpick.nitpick.dto.ApiResponse;
import com.example.nitpick.nitpick.dto.VenueRequest;
import com.example.nitpick.nitpick.dto.VenueResponse;
import com.example.nitpick.nitpick.service.VenueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/venues")
@RequiredArgsConstructor
public class VenueController {

    private final VenueService venueService;

    @PostMapping
    public ResponseEntity<ApiResponse<VenueResponse>> createVenue(@Valid @RequestBody VenueRequest request) {
        VenueResponse created = venueService.createVenue(request);
        return new ResponseEntity<>(ApiResponse.ofSuccess("Venue created successfully", created), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VenueResponse>> getVenueById(@PathVariable Long id) {
        VenueResponse venue = venueService.getVenueById(id);
        return ResponseEntity.ok(ApiResponse.ofSuccess(venue));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VenueResponse>>> getAllVenues(
            @RequestParam(required = false) Boolean activeOnly) {
        List<VenueResponse> venues = venueService.getAllVenues(activeOnly);
        return ResponseEntity.ok(ApiResponse.ofSuccess(venues));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VenueResponse>> updateVenue(
            @PathVariable Long id,
            @Valid @RequestBody VenueRequest request) {
        VenueResponse updated = venueService.updateVenue(id, request);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Venue updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivateVenue(@PathVariable Long id) {
        venueService.deactivateVenue(id);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Venue deactivated successfully", null));
    }
}
