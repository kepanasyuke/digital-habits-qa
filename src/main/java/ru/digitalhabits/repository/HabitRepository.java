package ru.digitalhabits.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.digitalhabits.domain.Habit;

import java.util.List;
import java.util.UUID;

public interface HabitRepository extends JpaRepository<Habit, UUID> {

    List<Habit> findByUserIdOrderByCreatedAtAsc(UUID userId);

    long countByUserId(UUID userId);
}
