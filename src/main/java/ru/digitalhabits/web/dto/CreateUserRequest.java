package ru.digitalhabits.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank(message = "email обязателен")
        @Email(message = "email имеет неверный формат")
        @Size(max = 255, message = "email длиннее 255 символов")
        String email,

        @NotBlank(message = "displayName обязателен")
        @Size(max = 255, message = "displayName длиннее 255 символов")
        String displayName
) {
}
