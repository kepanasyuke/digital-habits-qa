package ru.digitalhabits.support;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
public class DbClient {

    private final JdbcTemplate jdbcTemplate;

    public DbClient(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void truncateAll() {
        jdbcTemplate.execute("TRUNCATE TABLE checkins, habits, users CASCADE");
    }

    public long countCheckins(UUID habitId) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM checkins WHERE habit_id = ?", Long.class, habitId);
        return count == null ? 0 : count;
    }

    public long countCheckinsByDate(UUID habitId, LocalDate date) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM checkins WHERE habit_id = ? AND checkin_date = ?",
                Long.class, habitId, date);
        return count == null ? 0 : count;
    }

    public String habitStatus(UUID habitId) {
        return jdbcTemplate.queryForObject(
                "SELECT status FROM habits WHERE id = ?", String.class, habitId);
    }

    public long countHabits() {
        Long count = jdbcTemplate.queryForObject("SELECT count(*) FROM habits", Long.class);
        return count == null ? 0 : count;
    }

    public long countOrphanCheckins() {
        Long count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM checkins c LEFT JOIN habits h ON h.id = c.habit_id WHERE h.id IS NULL",
                Long.class);
        return count == null ? 0 : count;
    }
}
