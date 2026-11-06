package ru.digitalhabits.web.dto;

import java.time.LocalDate;
import java.util.UUID;

public record CheckinResponse(UUID id, UUID habitId, LocalDate date) {
}
