package ru.digitalhabits.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.digitalhabits.domain.Checkin;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface CheckinRepository extends JpaRepository<Checkin, UUID> {

    List<Checkin> findByHabitIdOrderByCheckinDateAsc(UUID habitId);

    boolean existsByHabitIdAndCheckinDate(UUID habitId, LocalDate checkinDate);

    long countByHabitId(UUID habitId);

    void deleteByHabitId(UUID habitId);
}
