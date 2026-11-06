package ru.digitalhabits.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateHabitRequest(
        @NotNull(message = "userId обязателен")
        UUID userId,

        @NotBlank(message = "title обязателен")
        @Size(max = 255, message = "title длиннее 255 символов")
        String title,

        @Min(value = 1, message = "targetPerWeek не может быть меньше 1")
        @Max(value = 7, message = "targetPerWeek не может быть больше 7")
        int targetPerWeek
) {
}
