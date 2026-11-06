package ru.digitalhabits.web.dto;

import ru.digitalhabits.domain.Habit;

import java.time.Instant;
import java.util.UUID;

public record HabitResponse(
        UUID id,
        UUID userId,
        String title,
        int targetPerWeek,
        String status,
        Instant createdAt
) {

    public static HabitResponse from(Habit habit) {
        return new HabitResponse(
                habit.getId(),
                habit.getUserId(),
                habit.getTitle(),
                habit.getTargetPerWeek(),
                habit.getStatus().name(),
                habit.getCreatedAt());
    }
}
