package com.example.nitpick.nitpick.controller;

import com.example.nitpick.nitpick.dto.ApiResponse;
import com.example.nitpick.nitpick.dto.EventRequest;
import com.example.nitpick.nitpick.dto.EventResponse;
import com.example.nitpick.nitpick.model.EventStatus;
import com.example.nitpick.nitpick.service.EventService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(@Valid @RequestBody EventRequest request) {
        EventResponse created = eventService.createEvent(request);
        return new ResponseEntity<>(ApiResponse.ofSuccess("Event created successfully", created), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> getEventById(@PathVariable Long id) {
        EventResponse event = eventService.getEventById(id);
        return ResponseEntity.ok(ApiResponse.ofSuccess(event));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EventResponse>>> getEvents(
            @RequestParam(required = false) Long venueId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) EventStatus status) {
        List<EventResponse> events = eventService.getEvents(venueId, date, status);
        return ResponseEntity.ok(ApiResponse.ofSuccess(events));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventRequest request) {
        EventResponse updated = eventService.updateEvent(id, request);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Event updated successfully", updated));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<EventResponse>> cancelEvent(@PathVariable Long id) {
        EventResponse cancelled = eventService.cancelEvent(id);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Event cancelled successfully", cancelled));
    }
}
