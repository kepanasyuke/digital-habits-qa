package ru.digitalhabits.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.digitalhabits.domain.Checkin;
import ru.digitalhabits.domain.Habit;
import ru.digitalhabits.domain.HabitStatus;
import ru.digitalhabits.service.HabitService;
import ru.digitalhabits.web.dto.CheckinResponse;
import ru.digitalhabits.web.dto.CreateCheckinRequest;
import ru.digitalhabits.web.dto.CreateHabitRequest;
import ru.digitalhabits.web.dto.HabitResponse;
import ru.digitalhabits.web.dto.HabitStatsResponse;
import ru.digitalhabits.web.dto.UpdateStatusRequest;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class HabitController {

    private final HabitService habitService;

    public HabitController(HabitService habitService) {
        this.habitService = habitService;
    }

    @PostMapping("/habits")
    @ResponseStatus(HttpStatus.CREATED)
    public HabitResponse create(@Valid @RequestBody CreateHabitRequest request) {
        return HabitResponse.from(habitService.createHabit(request));
    }

    @GetMapping("/habits/{id}")
    public HabitResponse get(@PathVariable UUID id) {
        return HabitResponse.from(habitService.getHabit(id));
    }

    @GetMapping("/users/{userId}/habits")
    public List<HabitResponse> listByUser(@PathVariable UUID userId) {
        return habitService.getUserHabits(userId).stream().map(HabitResponse::from).toList();
    }

    @PatchMapping("/habits/{id}/status")
    public HabitResponse changeStatus(@PathVariable UUID id, @Valid @RequestBody UpdateStatusRequest request) {
        Habit habit = habitService.changeStatus(id, HabitStatus.valueOf(request.status()));
        return HabitResponse.from(habit);
    }

    @DeleteMapping("/habits/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        habitService.deleteHabit(id);
    }

    @PostMapping("/habits/{id}/checkins")
    @ResponseStatus(HttpStatus.CREATED)
    public CheckinResponse checkin(@PathVariable UUID id, @Valid @RequestBody CreateCheckinRequest request) {
        Checkin checkin = habitService.addCheckin(id, request.date());
        return new CheckinResponse(checkin.getId(), checkin.getHabitId(), checkin.getCheckinDate());
    }

    @GetMapping("/habits/{id}/checkins")
    public List<CheckinResponse> checkins(@PathVariable UUID id) {
        return habitService.getCheckins(id).stream()
                .map(c -> new CheckinResponse(c.getId(), c.getHabitId(), c.getCheckinDate()))
                .toList();
    }

    @GetMapping("/habits/{id}/stats")
    public HabitStatsResponse stats(@PathVariable UUID id) {
        return habitService.getStats(id);
    }
}
