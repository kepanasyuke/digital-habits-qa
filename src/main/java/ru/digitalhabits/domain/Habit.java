package ru.digitalhabits.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "habits")
public class Habit {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String title;

    @Column(name = "target_per_week", nullable = false)
    private int targetPerWeek;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HabitStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Habit() {
    }

    public Habit(UUID id, UUID userId, String title, int targetPerWeek, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.targetPerWeek = targetPerWeek;
        this.status = HabitStatus.ACTIVE;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public int getTargetPerWeek() {
        return targetPerWeek;
    }

    public HabitStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void changeStatus(HabitStatus status) {
        this.status = status;
    }
}
