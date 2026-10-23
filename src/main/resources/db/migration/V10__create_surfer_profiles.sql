CREATE TABLE surfer_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id),
    skill_level VARCHAR(20) NOT NULL,
    preferred_location_id BIGINT REFERENCES surf_locations(id),
    preferred_lesson_type VARCHAR(20),
    budget_min NUMERIC(10,2),
    budget_max NUMERIC(10,2),
    board_type_preference VARCHAR(100),
    bio VARCHAR(2000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_surfer_profiles_user ON surfer_profiles (user_id);
