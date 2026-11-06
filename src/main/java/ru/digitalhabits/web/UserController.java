package ru.digitalhabits.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.digitalhabits.domain.User;
import ru.digitalhabits.service.HabitService;
import ru.digitalhabits.web.dto.CreateUserRequest;
import ru.digitalhabits.web.dto.UserResponse;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final HabitService habitService;

    public UserController(HabitService habitService) {
        this.habitService = habitService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
        User user = habitService.createUser(request);
        return new UserResponse(user.getId(), user.getEmail(), user.getDisplayName(), user.getCreatedAt());
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable UUID id) {
        User user = habitService.getUser(id);
        return new UserResponse(user.getId(), user.getEmail(), user.getDisplayName(), user.getCreatedAt());
    }
}
