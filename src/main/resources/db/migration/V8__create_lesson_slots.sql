CREATE TABLE lesson_slots (
    id BIGSERIAL PRIMARY KEY,
    lesson_id BIGINT NOT NULL REFERENCES lessons(id),
    instructor_id BIGINT NOT NULL REFERENCES instructors(id),
    location_id BIGINT REFERENCES surf_locations(id),
    start_time TIMESTAMPTZ NOT NULL,
    end_time TIMESTAMPTZ NOT NULL,
    capacity INTEGER NOT NULL,
    booked_count INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_slots_instructor_time ON lesson_slots (instructor_id, start_time, end_time);
CREATE INDEX idx_slots_lesson ON lesson_slots (lesson_id);
CREATE INDEX idx_slots_status ON lesson_slots (status);
