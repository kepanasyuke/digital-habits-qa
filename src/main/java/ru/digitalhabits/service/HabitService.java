package ru.digitalhabits.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.digitalhabits.domain.Checkin;
import ru.digitalhabits.domain.Habit;
import ru.digitalhabits.domain.HabitStatus;
import ru.digitalhabits.domain.User;
import ru.digitalhabits.repository.CheckinRepository;
import ru.digitalhabits.repository.HabitRepository;
import ru.digitalhabits.repository.UserRepository;
import ru.digitalhabits.web.dto.CreateHabitRequest;
import ru.digitalhabits.web.dto.CreateUserRequest;
import ru.digitalhabits.web.dto.HabitStatsResponse;
import ru.digitalhabits.web.error.BadRequestException;
import ru.digitalhabits.web.error.ConflictException;
import ru.digitalhabits.web.error.NotFoundException;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;

@Service
public class HabitService {

    private final UserRepository userRepository;
    private final HabitRepository habitRepository;
    private final CheckinRepository checkinRepository;
    private final HabitEventPublisher eventPublisher;

    public HabitService(UserRepository userRepository,
                        HabitRepository habitRepository,
                        CheckinRepository checkinRepository,
                        HabitEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.habitRepository = habitRepository;
        this.checkinRepository = checkinRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public User createUser(CreateUserRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ConflictException("Пользователь с email " + request.email() + " уже существует");
        }
        return userRepository.save(new User(UUID.randomUUID(), request.email(), request.displayName(), Instant.now()));
    }

    @Transactional(readOnly = true)
    public User getUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь " + id + " не найден"));
    }

    @Transactional
    public Habit createHabit(CreateHabitRequest request) {
        if (!userRepository.existsById(request.userId())) {
            throw new NotFoundException("Пользователь " + request.userId() + " не найден");
        }
        if (request.targetPerWeek() < 1 || request.targetPerWeek() > 7) {
            throw new BadRequestException("targetPerWeek должен быть в диапазоне 1..7");
        }
        Habit habit = new Habit(UUID.randomUUID(), request.userId(), request.title().trim(),
                request.targetPerWeek(), Instant.now());
        return habitRepository.save(habit);
    }

    @Transactional(readOnly = true)
    public Habit getHabit(UUID id) {
        return habitRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Привычка " + id + " не найдена"));
    }

    @Transactional(readOnly = true)
    public List<Habit> getUserHabits(UUID userId) {
        getUser(userId);
        return habitRepository.findByUserIdOrderByCreatedAtAsc(userId);
    }

    @Transactional
    public Habit changeStatus(UUID habitId, HabitStatus status) {
        Habit habit = getHabit(habitId);
        habit.changeStatus(status);
        return habit;
    }

    @Transactional
    public void deleteHabit(UUID habitId) {
        Habit habit = getHabit(habitId);
        checkinRepository.deleteByHabitId(habitId);
        habitRepository.delete(habit);
    }

    @Transactional
    public Checkin addCheckin(UUID habitId, LocalDate date) {
        Habit habit = getHabit(habitId);
        if (habit.getStatus() == HabitStatus.ARCHIVED) {
            throw new ConflictException("Нельзя отметить выполнение архивной привычки");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new BadRequestException("Дата отметки не может быть в будущем");
        }
        if (checkinRepository.existsByHabitIdAndCheckinDate(habitId, date)) {
            throw new ConflictException("Отметка за " + date + " уже существует");
        }
        Checkin checkin = checkinRepository.save(new Checkin(UUID.randomUUID(), habitId, date, Instant.now()));
        eventPublisher.checkinCreated(habit, checkin);
        return checkin;
    }

    @Transactional(readOnly = true)
    public List<Checkin> getCheckins(UUID habitId) {
        getHabit(habitId);
        return checkinRepository.findByHabitIdOrderByCheckinDateAsc(habitId);
    }

    @Transactional(readOnly = true)
    public HabitStatsResponse getStats(UUID habitId) {
        Habit habit = getHabit(habitId);
        List<Checkin> checkins = checkinRepository.findByHabitIdOrderByCheckinDateAsc(habitId);
        long total = checkins.size();

        LocalDate lastDate = checkins.isEmpty() ? null : checkins.get(checkins.size() - 1).getCheckinDate();

        java.util.Set<LocalDate> dates = checkins.stream()
                .map(Checkin::getCheckinDate)
                .collect(java.util.stream.Collectors.toSet());

        int streak = 0;
        if (lastDate != null) {
            LocalDate today = LocalDate.now();
            if (lastDate.equals(today) || lastDate.equals(today.minusDays(1))) {
                LocalDate cursor = lastDate;
                while (dates.contains(cursor)) {
                    streak++;
                    cursor = cursor.minusDays(1);
                }
            }
        }

        LocalDate weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);
        int weekCheckins = (int) checkins.stream()
                .filter(c -> !c.getCheckinDate().isBefore(weekStart) && !c.getCheckinDate().isAfter(weekEnd))
                .count();

        double completionRate = Math.min(1.0, (double) weekCheckins / habit.getTargetPerWeek());

        return new HabitStatsResponse(
                habit.getId(),
                total,
                streak,
                weekCheckins,
                habit.getTargetPerWeek(),
                Math.round(completionRate * 100.0) / 100.0,
                lastDate);
    }
}
