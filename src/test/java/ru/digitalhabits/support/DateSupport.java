package ru.digitalhabits.support;

import java.time.LocalDate;

public final class DateSupport {

    private DateSupport() {
    }

    public static LocalDate resolve(String token) {
        return switch (token.trim().toLowerCase()) {
            case "сегодня", "today" -> LocalDate.now();
            case "вчера", "yesterday" -> LocalDate.now().minusDays(1);
            case "позавчера" -> LocalDate.now().minusDays(2);
            case "завтра", "tomorrow" -> LocalDate.now().plusDays(1);
            default -> LocalDate.parse(token.trim());
        };
    }

    public static String isoToday() {
        return LocalDate.now().toString();
    }
}
