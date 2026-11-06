package ru.digitalhabits.support;

import io.cucumber.spring.ScenarioScope;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@ScenarioScope
public class ScenarioContext {

    private UUID userId;
    private UUID habitId;
    private String habitTitle;
    private String userEmail;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getHabitId() {
        return habitId;
    }

    public void setHabitId(UUID habitId) {
        this.habitId = habitId;
    }

    public String getHabitTitle() {
        return habitTitle;
    }

    public void setHabitTitle(String habitTitle) {
        this.habitTitle = habitTitle;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
