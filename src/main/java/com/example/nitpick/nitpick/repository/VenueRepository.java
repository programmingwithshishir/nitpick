package com.example.nitpick.nitpick.repository;

import com.example.nitpick.nitpick.model.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {
    List<Venue> findByIsActive(Boolean isActive);
    List<Venue> findByNameContainingIgnoreCase(String name);
}
