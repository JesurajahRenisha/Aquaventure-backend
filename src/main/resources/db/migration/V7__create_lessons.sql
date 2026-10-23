CREATE TABLE lessons (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL REFERENCES surf_schools(id),
    title VARCHAR(150) NOT NULL,
    description VARCHAR(2000),
    lesson_type VARCHAR(20) NOT NULL,
    skill_level VARCHAR(20) NOT NULL,
    duration_minutes INTEGER NOT NULL,
    price NUMERIC(10,2) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    max_participants INTEGER NOT NULL,
    includes_equipment BOOLEAN NOT NULL DEFAULT false,
    location_id BIGINT REFERENCES surf_locations(id),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_lessons_school ON lessons (school_id);
CREATE INDEX idx_lessons_active ON lessons (active);
