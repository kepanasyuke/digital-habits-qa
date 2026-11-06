package ru.digitalhabits.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateStatusRequest(
        @NotBlank(message = "status обязателен")
        @Pattern(regexp = "ACTIVE|ARCHIVED", message = "status допускает только ACTIVE или ARCHIVED")
        String status
) {
}
