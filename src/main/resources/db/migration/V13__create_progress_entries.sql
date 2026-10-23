CREATE TABLE progress_entries (
    id BIGSERIAL PRIMARY KEY,
    surfer_user_id BIGINT NOT NULL REFERENCES users(id),
    booking_id BIGINT REFERENCES bookings(id),
    date TIMESTAMPTZ NOT NULL,
    location_id BIGINT REFERENCES surf_locations(id),
    skill_focus VARCHAR(150),
    duration_minutes INTEGER,
    self_rating INTEGER,
    notes VARCHAR(2000),
    instructor_feedback VARCHAR(2000),
    is_draft BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_progress_entries_surfer ON progress_entries (surfer_user_id);
CREATE INDEX idx_progress_entries_booking ON progress_entries (booking_id);
