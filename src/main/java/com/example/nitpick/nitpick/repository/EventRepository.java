package com.example.nitpick.nitpick.repository;

import com.example.nitpick.nitpick.model.Event;
import com.example.nitpick.nitpick.model.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByVenueId(Long venueId);

    List<Event> findByEventDate(LocalDate eventDate);

    List<Event> findByStatus(EventStatus status);

    List<Event> findByVenueIdAndEventDate(Long venueId, LocalDate eventDate);

    @Query("SELECT e FROM Event e WHERE e.venue.id = :venueId " +
           "AND e.eventDate = :eventDate " +
           "AND e.status = :status " +
           "AND e.startTime < :endTime " +
           "AND e.endTime > :startTime " +
           "AND (:excludeEventId IS NULL OR e.id != :excludeEventId)")
    List<Event> findConflictingEvents(
            @Param("venueId") Long venueId,
            @Param("eventDate") LocalDate eventDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("status") EventStatus status,
            @Param("excludeEventId") Long excludeEventId
    );
}
