package ru.digitalhabits.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "checkins")
public class Checkin {

    @Id
    private UUID id;

    @Column(name = "habit_id", nullable = false)
    private UUID habitId;

    @Column(name = "checkin_date", nullable = false)
    private LocalDate checkinDate;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Checkin() {
    }

    public Checkin(UUID id, UUID habitId, LocalDate checkinDate, Instant createdAt) {
        this.id = id;
        this.habitId = habitId;
        this.checkinDate = checkinDate;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getHabitId() {
        return habitId;
    }

    public LocalDate getCheckinDate() {
        return checkinDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
