package com.example.nitpick.nitpick;

import com.example.nitpick.nitpick.dto.EventRequest;
import com.example.nitpick.nitpick.dto.RegistrationRequest;
import com.example.nitpick.nitpick.dto.UserRequest;
import com.example.nitpick.nitpick.dto.VenueRequest;
import com.example.nitpick.nitpick.model.EventStatus;
import com.example.nitpick.nitpick.model.Role;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class VenueBookingIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Order(1)
    void shouldCreateUserSuccessfully() throws Exception {
        UserRequest userRequest = UserRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .studentId("STU1001")
                .password("secret123")
                .role(Role.STUDENT)
                .build();

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.data.role").value("STUDENT"));
    }

    @Test
    @Order(2)
    void shouldRejectDuplicateUserEmail() throws Exception {
        UserRequest userRequest = UserRequest.builder()
                .firstName("Jane")
                .lastName("Doe")
                .email("john.doe@example.com")
                .studentId("STU1002")
                .role(Role.STUDENT)
                .build();

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @Order(3)
    void shouldCreateVenueSuccessfully() throws Exception {
        VenueRequest venueRequest = VenueRequest.builder()
                .name("Main Auditorium")
                .capacity(100)
                .location("Block A, 1st Floor")
                .description("Air-conditioned auditorium with projector")
                .isActive(true)
                .build();

        mockMvc.perform(post("/api/venues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(venueRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.name").value("Main Auditorium"))
                .andExpect(jsonPath("$.data.capacity").value(100));
    }

    @Test
    @Order(4)
    void shouldCreateEventSuccessfully() throws Exception {
        EventRequest eventRequest = EventRequest.builder()
                .venueId(1L)
                .name("Tech Symposium 2026")
                .description("Annual computer science symposium")
                .eventDate(LocalDate.now().plusDays(10))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(12, 0))
                .totalSlots(2)
                .status(EventStatus.ACTIVE)
                .build();

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.name").value("Tech Symposium 2026"))
                .andExpect(jsonPath("$.data.totalSlots").value(2))
                .andExpect(jsonPath("$.data.availableSlots").value(2));
    }

    @Test
    @Order(5)
    void shouldRejectOverlappingEventAtSameVenueAndTime() throws Exception {
        // Same venue, same date, overlapping time (11:00 to 13:00 overlaps with 10:00 to 12:00)
        EventRequest conflictingRequest = EventRequest.builder()
                .venueId(1L)
                .name("Overlapping Workshop")
                .eventDate(LocalDate.now().plusDays(10))
                .startTime(LocalTime.of(11, 0))
                .endTime(LocalTime.of(13, 0))
                .totalSlots(10)
                .status(EventStatus.ACTIVE)
                .build();

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(conflictingRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @Order(6)
    void shouldRegisterUserForEventSlotSuccessfully() throws Exception {
        RegistrationRequest regRequest = RegistrationRequest.builder()
                .eventId(1L)
                .userId(1L)
                .build();

        mockMvc.perform(post("/api/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.slotNumber").value(1))
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));

        // Verify available slots decreased
        mockMvc.perform(get("/api/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bookedSlots").value(1))
                .andExpect(jsonPath("$.data.availableSlots").value(1));
    }

    @Test
    @Order(7)
    void shouldRejectDuplicateRegistrationForSameUserAndEvent() throws Exception {
        RegistrationRequest duplicateRequest = RegistrationRequest.builder()
                .eventId(1L)
                .userId(1L)
                .build();

        mockMvc.perform(post("/api/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("already registered")));
    }

    @Test
    @Order(8)
    void shouldRegisterSecondUserAndThenRejectWhenFull() throws Exception {
        // Create second user
        UserRequest secondUser = UserRequest.builder()
                .firstName("Alice")
                .lastName("Smith")
                .email("alice.smith@example.com")
                .studentId("STU1002")
                .role(Role.STUDENT)
                .build();

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondUser)))
                .andExpect(status().isCreated());

        // Book slot 2
        RegistrationRequest reg2 = RegistrationRequest.builder()
                .eventId(1L)
                .userId(2L)
                .build();

        mockMvc.perform(post("/api/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.slotNumber").value(2));

        // Create third user
        UserRequest thirdUser = UserRequest.builder()
                .firstName("Bob")
                .lastName("Jones")
                .email("bob.jones@example.com")
                .studentId("STU1003")
                .role(Role.STUDENT)
                .build();

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(thirdUser)))
                .andExpect(status().isCreated());

        // Book slot 3 - should fail because totalSlots is 2!
        RegistrationRequest reg3 = RegistrationRequest.builder()
                .eventId(1L)
                .userId(3L)
                .build();

        mockMvc.perform(post("/api/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg3)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("fully booked")));
    }

    @Test
    @Order(9)
    void shouldCancelRegistrationAndFreeSlot() throws Exception {
        // Cancel registration 1
        mockMvc.perform(post("/api/registrations/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));

        // Check event available slots is back to 1
        mockMvc.perform(get("/api/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bookedSlots").value(1))
                .andExpect(jsonPath("$.data.availableSlots").value(1));

        // Now user 3 can book the freed slot!
        RegistrationRequest reg3 = RegistrationRequest.builder()
                .eventId(1L)
                .userId(3L)
                .build();

        mockMvc.perform(post("/api/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg3)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.slotNumber").value(1));
    }
}
