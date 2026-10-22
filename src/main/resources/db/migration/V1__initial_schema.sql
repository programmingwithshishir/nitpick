-- Flyway Migration V1: Initial schema for venue booking application (MySQL compatible)

-- Drop tables if they exist (for clean reset on each run due to flyway.reset-on-migration=true)
DROP TABLE IF EXISTS registrations;
DROP TABLE IF EXISTS events;
DROP TABLE IF EXISTS venues;
DROP TABLE IF EXISTS users;

-- Create users table (students/staff)
CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    student_id VARCHAR(50) UNIQUE,
    password VARCHAR(255),
    role VARCHAR(50) DEFAULT 'STUDENT' CHECK (role IN ('STUDENT', 'ADMIN', 'FACULTY')),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create venues table
CREATE TABLE venues (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    capacity INTEGER NOT NULL CHECK (capacity > 0),
    location VARCHAR(500),
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create events table with concurrency-safe slot management
CREATE TABLE events (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    venue_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    event_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    total_slots INTEGER NOT NULL CHECK (total_slots > 0),
    booked_slots INTEGER DEFAULT 0,
    status VARCHAR(50) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'CANCELLED', 'COMPLETED')),
    version INTEGER DEFAULT 0, -- Optimistic locking version for concurrency safety
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key constraint to venues
    CONSTRAINT fk_event_venue FOREIGN KEY (venue_id) REFERENCES venues(id) ON DELETE CASCADE,
    
    -- Unique constraint to prevent double-booking same slot time
    CONSTRAINT unique_event_time_slot UNIQUE (venue_id, event_date, start_time, end_time)
);

-- Create registrations table with concurrency safety
CREATE TABLE registrations (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    slot_number INTEGER, -- Specific slot number within the event
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) DEFAULT 'CONFIRMED' CHECK (status IN ('CONFIRMED', 'CANCELLED', 'PAID', 'WAITLIST')),
    
    -- Foreign key constraints
    CONSTRAINT fk_registration_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    CONSTRAINT fk_registration_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- Concurrency safety: prevent double-booking same slot by same user
    CONSTRAINT unique_user_event_slot UNIQUE (event_id, user_id, slot_number),
    
    -- Indexes for fast lookup
    INDEX idx_registrations_event (event_id),
    INDEX idx_registrations_user (user_id)
);

-- Create indexes for frequent query patterns
CREATE INDEX idx_events_venue_date ON events(venue_id, event_date);
CREATE INDEX idx_events_date ON events(event_date);
CREATE INDEX idx_venues_active ON venues(is_active);
CREATE INDEX idx_events_active ON events(status);

-- Schema metadata:
-- venues: College venues available for booking
-- events: Events scheduled in venues with slot management
-- registrations: Student registrations for event slots with concurrency safety
-- events.version: Optimistic locking version for concurrent update safety
