CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    surfer_user_id BIGINT NOT NULL REFERENCES users(id),
    school_id BIGINT NOT NULL REFERENCES surf_schools(id),
    lesson_slot_id BIGINT NOT NULL REFERENCES lesson_slots(id),
    instructor_id BIGINT NOT NULL REFERENCES instructors(id),
    participants INTEGER NOT NULL,
    total_price NUMERIC(10,2) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    surfer_note VARCHAR(1000),
    status VARCHAR(20) NOT NULL,
    school_response_note VARCHAR(1000),
    responded_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_bookings_surfer ON bookings (surfer_user_id);
CREATE INDEX idx_bookings_school ON bookings (school_id);
CREATE INDEX idx_bookings_slot ON bookings (lesson_slot_id);
CREATE INDEX idx_bookings_status ON bookings (status);
