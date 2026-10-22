package com.example.nitpick.nitpick.controller;

import com.example.nitpick.nitpick.dto.ApiResponse;
import com.example.nitpick.nitpick.dto.RegistrationRequest;
import com.example.nitpick.nitpick.dto.RegistrationResponse;
import com.example.nitpick.nitpick.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/registrations")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping
    public ResponseEntity<ApiResponse<RegistrationResponse>> registerForEvent(
            @Valid @RequestBody RegistrationRequest request) {
        RegistrationResponse registration = registrationService.registerForEvent(request);
        return new ResponseEntity<>(ApiResponse.ofSuccess("Registration successful", registration), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RegistrationResponse>> getRegistrationById(@PathVariable Long id) {
        RegistrationResponse registration = registrationService.getRegistrationById(id);
        return ResponseEntity.ok(ApiResponse.ofSuccess(registration));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<RegistrationResponse>> cancelRegistration(@PathVariable Long id) {
        RegistrationResponse cancelled = registrationService.cancelRegistration(id);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Registration cancelled successfully", cancelled));
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<ApiResponse<List<RegistrationResponse>>> getRegistrationsByEvent(@PathVariable Long eventId) {
        List<RegistrationResponse> registrations = registrationService.getRegistrationsByEvent(eventId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(registrations));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<RegistrationResponse>>> getRegistrationsByUser(@PathVariable Long userId) {
        List<RegistrationResponse> registrations = registrationService.getRegistrationsByUser(userId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(registrations));
    }
}
