-- Restructures the surf-lessons schema (V1/V2) into the Smart Surf Tourism
-- Management System schema. Real user accounts (name/email/password/role)
-- are preserved and migrated into the new role-profile tables; provider
-- business names carry forward from provider_profiles. All other previous
-- domain data (lessons, bookings, reviews, messages, etc.) belonged to a
-- different, now-superseded data model and is intentionally not migrated.

-- 1. Restructure users: firstname+lastname -> name, add phone_number,
--    rename id -> user_id to match the new entity's field name.
ALTER TABLE users ADD COLUMN IF NOT EXISTS name VARCHAR(150);
UPDATE users SET name = TRIM(BOTH ' ' FROM COALESCE(firstname, '') || ' ' || COALESCE(lastname, '')) WHERE name IS NULL;
ALTER TABLE users ALTER COLUMN name SET NOT NULL;
ALTER TABLE users ADD COLUMN IF NOT EXISTS phone_number VARCHAR(30);
ALTER TABLE users DROP COLUMN IF EXISTS firstname;
ALTER TABLE users DROP COLUMN IF EXISTS lastname;
ALTER TABLE users RENAME COLUMN id TO user_id;

-- 2. Create the new role-profile and domain tables.
CREATE TABLE tourist (
    tourist_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users (user_id),
    skill_level VARCHAR(20),
    experience VARCHAR(500)
);

CREATE TABLE provider (
    provider_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users (user_id),
    business_name VARCHAR(150),
    contact_details VARCHAR(255),
    location VARCHAR(255)
);

CREATE TABLE admin (
    admin_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users (user_id)
);

CREATE TABLE instructor (
    instructor_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE REFERENCES users (user_id),
    provider_id BIGINT NOT NULL REFERENCES provider (provider_id),
    name VARCHAR(150),
    certification VARCHAR(255),
    experience VARCHAR(500),
    availability BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE equipment (
    equipment_id BIGSERIAL PRIMARY KEY,
    provider_id BIGINT NOT NULL REFERENCES provider (provider_id),
    equipment_name VARCHAR(150),
    quantity INTEGER,
    availability BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE surf_location (
    location_id BIGSERIAL PRIMARY KEY,
    location_name VARCHAR(150) NOT NULL,
    difficulty_level VARCHAR(20),
    safety_rating INTEGER
);

CREATE TABLE surf_activity (
    activity_id BIGSERIAL PRIMARY KEY,
    provider_id BIGINT NOT NULL REFERENCES provider (provider_id),
    location_id BIGINT NOT NULL REFERENCES surf_location (location_id),
    activity_name VARCHAR(150) NOT NULL,
    price NUMERIC(10, 2),
    duration INTEGER,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE booking (
    booking_id BIGSERIAL PRIMARY KEY,
    tourist_id BIGINT NOT NULL REFERENCES tourist (tourist_id),
    activity_id BIGINT NOT NULL REFERENCES surf_activity (activity_id),
    booking_date TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    payment_status VARCHAR(20) NOT NULL DEFAULT 'UNPAID',
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE surf_progress (
    progress_id BIGSERIAL PRIMARY KEY,
    tourist_id BIGINT NOT NULL REFERENCES tourist (tourist_id),
    instructor_id BIGINT REFERENCES instructor (instructor_id),
    session_date DATE,
    skill_level VARCHAR(20),
    notes VARCHAR(1000)
);

CREATE TABLE recommendation (
    recommendation_id BIGSERIAL PRIMARY KEY,
    tourist_id BIGINT NOT NULL REFERENCES tourist (tourist_id),
    location_id BIGINT NOT NULL REFERENCES surf_location (location_id),
    recommendation_reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE weather_information (
    weather_id BIGSERIAL PRIMARY KEY,
    location_id BIGINT NOT NULL REFERENCES surf_location (location_id),
    wave_height DOUBLE PRECISION,
    wind_speed DOUBLE PRECISION,
    temperature DOUBLE PRECISION,
    recorded_at TIMESTAMP NOT NULL DEFAULT now()
);

-- 3. Migrate real user data into the new role-profile tables.
INSERT INTO tourist (user_id, skill_level, experience)
SELECT user_id, 'BEGINNER', NULL FROM users WHERE role = 'SURFER';

INSERT INTO provider (user_id, business_name, contact_details, location)
SELECT u.user_id, COALESCE(pp.business_name, u.name), NULL, NULL
FROM users u LEFT JOIN provider_profiles pp ON pp.user_id = u.user_id
WHERE u.role = 'PROVIDER';

INSERT INTO admin (user_id)
SELECT user_id FROM users WHERE role = 'ADMIN';

-- 4. Drop tables from the previous (surf-lessons) schema; that domain
--    model is superseded and its demo/seed data does not carry forward.
DROP TABLE IF EXISTS messages;
DROP TABLE IF EXISTS conversations;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS surf_conditions;
DROP TABLE IF EXISTS favorite_spots;
DROP TABLE IF EXISTS surf_sessions;
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS availability_slots;
DROP TABLE IF EXISTS lessons;
DROP TABLE IF EXISTS provider_profiles;
DROP TABLE IF EXISTS surf_spots;
DROP TABLE IF EXISTS platform_settings;
