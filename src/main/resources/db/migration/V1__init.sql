-- AquaVenture (surf lessons) core schema
--
-- users is created IF NOT EXISTS + ALTERed rather than plain CREATE TABLE
-- because some environments already have a legacy `users` table (and an
-- unused `customer` table) from before this schema existed. This adapts
-- that legacy table in place instead of dropping real registered accounts;
-- on a genuinely empty database this block just creates the full table.

DROP TABLE IF EXISTS customer;

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    firstname VARCHAR(50) NOT NULL,
    lastname VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL
);

ALTER TABLE users ADD COLUMN IF NOT EXISTS role VARCHAR(20) NOT NULL DEFAULT 'SURFER';
ALTER TABLE users ADD COLUMN IF NOT EXISTS enabled BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE users ADD CONSTRAINT users_email_key UNIQUE (email);

CREATE TABLE provider_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users (id),
    business_name VARCHAR(150),
    bio VARCHAR(2000),
    verification_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    applied_at TIMESTAMP NOT NULL DEFAULT now(),
    decided_at TIMESTAMP,
    decided_by_admin_id BIGINT
);

CREATE TABLE surf_spots (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    region VARCHAR(150) NOT NULL,
    description VARCHAR(2000),
    difficulty_level VARCHAR(20) NOT NULL,
    image_url VARCHAR(500)
);

CREATE TABLE lessons (
    id BIGSERIAL PRIMARY KEY,
    provider_profile_id BIGINT NOT NULL REFERENCES provider_profiles (id),
    surf_spot_id BIGINT NOT NULL REFERENCES surf_spots (id),
    title VARCHAR(150) NOT NULL,
    description VARCHAR(2000),
    level VARCHAR(20) NOT NULL,
    duration_minutes INTEGER NOT NULL,
    capacity INTEGER NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE availability_slots (
    id BIGSERIAL PRIMARY KEY,
    lesson_id BIGINT NOT NULL REFERENCES lessons (id),
    date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    capacity INTEGER NOT NULL,
    booked_count INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN'
);

CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    surfer_id BIGINT NOT NULL REFERENCES users (id),
    lesson_id BIGINT NOT NULL REFERENCES lessons (id),
    slot_id BIGINT NOT NULL REFERENCES availability_slots (id),
    num_guests INTEGER NOT NULL,
    total_price NUMERIC(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    cancellation_reason VARCHAR(500),
    refund_percentage INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL UNIQUE REFERENCES bookings (id),
    surfer_id BIGINT NOT NULL REFERENCES users (id),
    provider_profile_id BIGINT NOT NULL REFERENCES provider_profiles (id),
    rating INTEGER NOT NULL,
    comment VARCHAR(2000),
    moderation_status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE surf_sessions (
    id BIGSERIAL PRIMARY KEY,
    surfer_id BIGINT NOT NULL REFERENCES users (id),
    surf_spot_id BIGINT REFERENCES surf_spots (id),
    booking_id BIGINT REFERENCES bookings (id),
    session_date DATE NOT NULL,
    duration_minutes INTEGER,
    wave_height_m DOUBLE PRECISION,
    notes VARCHAR(2000)
);

CREATE TABLE favorite_spots (
    id BIGSERIAL PRIMARY KEY,
    surfer_id BIGINT NOT NULL REFERENCES users (id),
    surf_spot_id BIGINT NOT NULL REFERENCES surf_spots (id),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (surfer_id, surf_spot_id)
);

CREATE TABLE surf_conditions (
    id BIGSERIAL PRIMARY KEY,
    surf_spot_id BIGINT NOT NULL REFERENCES surf_spots (id),
    recorded_at TIMESTAMP NOT NULL DEFAULT now(),
    wave_height_m DOUBLE PRECISION,
    wind_speed_kmh DOUBLE PRECISION,
    tide VARCHAR(50),
    uv_index INTEGER,
    visibility VARCHAR(50),
    temperature_c DOUBLE PRECISION,
    safety_flag VARCHAR(20) NOT NULL
);

CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users (id),
    type VARCHAR(30) NOT NULL,
    title VARCHAR(200),
    message VARCHAR(1000),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE conversations (
    id BIGSERIAL PRIMARY KEY,
    participant_a_id BIGINT NOT NULL REFERENCES users (id),
    participant_b_id BIGINT NOT NULL REFERENCES users (id),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE messages (
    id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL REFERENCES conversations (id),
    sender_id BIGINT NOT NULL REFERENCES users (id),
    body VARCHAR(4000) NOT NULL,
    sent_at TIMESTAMP NOT NULL DEFAULT now(),
    read_at TIMESTAMP
);

CREATE TABLE platform_settings (
    id BIGSERIAL PRIMARY KEY,
    setting_key VARCHAR(100) NOT NULL UNIQUE,
    setting_value VARCHAR(1000)
);

CREATE INDEX idx_lessons_provider ON lessons (provider_profile_id);
CREATE INDEX idx_slots_lesson ON availability_slots (lesson_id);
CREATE INDEX idx_bookings_surfer ON bookings (surfer_id);
CREATE INDEX idx_bookings_lesson ON bookings (lesson_id);
CREATE INDEX idx_conditions_spot ON surf_conditions (surf_spot_id);
CREATE INDEX idx_notifications_user ON notifications (user_id);
