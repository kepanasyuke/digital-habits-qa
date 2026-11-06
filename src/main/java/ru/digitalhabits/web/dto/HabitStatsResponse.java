package ru.digitalhabits.web.dto;

import java.time.LocalDate;
import java.util.UUID;

public record HabitStatsResponse(
        UUID habitId,
        long checkinsTotal,
        int currentStreak,
        int weekCheckins,
        int targetPerWeek,
        double completionRate,
        LocalDate lastCheckinDate
) {
}
