package ru.digitalhabits.web.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateCheckinRequest(
        @NotNull(message = "date обязательна")
        LocalDate date
) {
}
