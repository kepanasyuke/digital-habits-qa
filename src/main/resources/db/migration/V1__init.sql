CREATE TABLE users (
    id           UUID PRIMARY KEY,
    email        VARCHAR(255) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_users_email UNIQUE (email)
);

CREATE TABLE habits (
    id             UUID PRIMARY KEY,
    user_id        UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    title          VARCHAR(255) NOT NULL,
    target_per_week INTEGER     NOT NULL CHECK (target_per_week BETWEEN 1 AND 7),
    status         VARCHAR(16)  NOT NULL CHECK (status IN ('ACTIVE', 'ARCHIVED')),
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX ix_habits_user_id ON habits (user_id);

CREATE TABLE checkins (
    id           UUID PRIMARY KEY,
    habit_id     UUID        NOT NULL REFERENCES habits (id) ON DELETE CASCADE,
    checkin_date DATE        NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_checkins_habit_date UNIQUE (habit_id, checkin_date)
);

CREATE INDEX ix_checkins_habit_id ON checkins (habit_id);
