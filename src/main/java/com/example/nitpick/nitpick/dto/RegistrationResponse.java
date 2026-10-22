package com.example.nitpick.nitpick.dto;

import com.example.nitpick.nitpick.model.RegistrationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
}
