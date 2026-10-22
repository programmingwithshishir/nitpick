package com.example.nitpick.nitpick.repository;

import com.example.nitpick.nitpick.model.Registration;
import com.example.nitpick.nitpick.model.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    List<Registration> findByEventId(Long eventId);

    List<Registration> findByUserId(Long userId);

    Optional<Registration> findByEventIdAndUserId(Long eventId, Long userId);

    Optional<Registration> findByEventIdAndUserIdAndStatus(Long eventId, Long userId, RegistrationStatus status);

    boolean existsByEventIdAndUserIdAndStatus(Long eventId, Long userId, RegistrationStatus status);

    boolean existsByEventIdAndSlotNumberAndStatusNot(Long eventId, Integer slotNumber, RegistrationStatus status);

    long countByEventIdAndStatus(Long eventId, RegistrationStatus status);

    @Query("SELECT r.slotNumber FROM Registration r WHERE r.event.id = :eventId " +
           "AND r.status != :cancelledStatus AND r.slotNumber IS NOT NULL")
    Set<Integer> findBookedSlotNumbers(
            @Param("eventId") Long eventId,
            @Param("cancelledStatus") RegistrationStatus cancelledStatus
    );
}
