-- Seeds sample data for entities introduced by the new tourism-system
-- schema (V3) that didn't exist in the previous design: surf locations,
-- surf activities, an instructor, equipment, a booking, progress, a
-- recommendation, and a weather reading. Looks up the tourist/provider rows
-- migrated in V3 by email rather than assuming fixed ids.

DO $$
DECLARE
    ravi_provider_id BIGINT;
    priya_provider_id BIGINT;
    kasun_tourist_id BIGINT;
    amaya_tourist_id BIGINT;
    instructor_user_id BIGINT;
    v_instructor_id BIGINT;
    spot_main_point BIGINT;
    spot_peanut_farm BIGINT;
    spot_whiskey_point BIGINT;
    activity_beginner BIGINT;
    activity_intermediate BIGINT;
    booking1 BIGINT;
    password_hash CONSTANT VARCHAR := '$2a$10$C1rXpJWO7/dwYiNqVC3Ih.hSFqTnKL1Gr21o97fMS2XDcBla31/VC';
BEGIN
    SELECT p.provider_id INTO ravi_provider_id FROM provider p JOIN users u ON u.user_id = p.user_id WHERE u.email = 'ravi@aquaventure.com';
    SELECT p.provider_id INTO priya_provider_id FROM provider p JOIN users u ON u.user_id = p.user_id WHERE u.email = 'priya@aquaventure.com';
    SELECT t.tourist_id INTO kasun_tourist_id FROM tourist t JOIN users u ON u.user_id = t.user_id WHERE u.email = 'kasun@example.com';
    SELECT t.tourist_id INTO amaya_tourist_id FROM tourist t JOIN users u ON u.user_id = t.user_id WHERE u.email = 'amaya@example.com';

    -- Give Kasun and Amaya distinct skill levels so recommendations differ.
    UPDATE tourist SET skill_level = 'BEGINNER' WHERE tourist_id = kasun_tourist_id;
    UPDATE tourist SET skill_level = 'INTERMEDIATE' WHERE tourist_id = amaya_tourist_id;

    -- A dedicated instructor login, employed by Ravi's provider account.
    IF ravi_provider_id IS NOT NULL THEN
        INSERT INTO users (name, email, password, phone_number, role, enabled, created_at)
        VALUES ('Nadia Silva', 'nadia.instructor@aquaventure.com', password_hash, NULL, 'INSTRUCTOR', TRUE, now() - INTERVAL '40 days')
        RETURNING user_id INTO instructor_user_id;

        INSERT INTO instructor (user_id, provider_id, name, certification, experience, availability)
        VALUES (instructor_user_id, ravi_provider_id, 'Nadia Silva', 'ISA Level 2 Surf Coach', '6 years teaching beginners', TRUE)
        RETURNING instructor_id INTO v_instructor_id;

        INSERT INTO equipment (provider_id, equipment_name, quantity, availability)
        VALUES (ravi_provider_id, 'Soft-top longboard', 8, TRUE);
        INSERT INTO equipment (provider_id, equipment_name, quantity, availability)
        VALUES (ravi_provider_id, 'Rash guard (assorted sizes)', 15, TRUE);
    END IF;

    IF priya_provider_id IS NOT NULL THEN
        INSERT INTO equipment (provider_id, equipment_name, quantity, availability)
        VALUES (priya_provider_id, 'Performance shortboard', 4, TRUE);
    END IF;

    -- Surf locations
    INSERT INTO surf_location (location_name, difficulty_level, safety_rating)
    VALUES ('Main Point', 'BEGINNER', 5) RETURNING location_id INTO spot_main_point;

    INSERT INTO surf_location (location_name, difficulty_level, safety_rating)
    VALUES ('Peanut Farm', 'INTERMEDIATE', 4) RETURNING location_id INTO spot_peanut_farm;

    INSERT INTO surf_location (location_name, difficulty_level, safety_rating)
    VALUES ('Whiskey Point', 'ADVANCED', 3) RETURNING location_id INTO spot_whiskey_point;

    -- Surf activities
    IF ravi_provider_id IS NOT NULL THEN
        INSERT INTO surf_activity (provider_id, location_id, activity_name, price, duration, active)
        VALUES (ravi_provider_id, spot_main_point, 'Beginner Surf Lesson', 25.00, 120, TRUE)
        RETURNING activity_id INTO activity_beginner;

        INSERT INTO surf_activity (provider_id, location_id, activity_name, price, duration, active)
        VALUES (ravi_provider_id, spot_peanut_farm, 'Intermediate Wave Coaching', 35.00, 150, TRUE)
        RETURNING activity_id INTO activity_intermediate;
    END IF;

    IF priya_provider_id IS NOT NULL THEN
        INSERT INTO surf_activity (provider_id, location_id, activity_name, price, duration, active)
        VALUES (priya_provider_id, spot_whiskey_point, 'Advanced Point Break Session', 45.00, 180, TRUE);
    END IF;

    -- A confirmed, paid booking for Kasun
    IF kasun_tourist_id IS NOT NULL AND activity_beginner IS NOT NULL THEN
        INSERT INTO booking (tourist_id, activity_id, booking_date, status, payment_status, created_at)
        VALUES (kasun_tourist_id, activity_beginner, now() + INTERVAL '3 days', 'CONFIRMED', 'PAID', now() - INTERVAL '2 days')
        RETURNING booking_id INTO booking1;
    END IF;

    -- A pending booking for Amaya
    IF amaya_tourist_id IS NOT NULL AND activity_intermediate IS NOT NULL THEN
        INSERT INTO booking (tourist_id, activity_id, booking_date, status, payment_status, created_at)
        VALUES (amaya_tourist_id, activity_intermediate, now() + INTERVAL '5 days', 'PENDING', 'UNPAID', now() - INTERVAL '1 days');
    END IF;

    -- A logged progress session for Kasun
    IF kasun_tourist_id IS NOT NULL THEN
        INSERT INTO surf_progress (tourist_id, instructor_id, session_date, skill_level, notes)
        VALUES (kasun_tourist_id, v_instructor_id, CURRENT_DATE - 10, 'BEGINNER', 'Caught my first green wave!');
    END IF;

    -- A sample weather reading and recommendation for Main Point
    INSERT INTO weather_information (location_id, wave_height, wind_speed, temperature, recorded_at)
    VALUES (spot_main_point, 0.8, 12, 29, now());

    IF kasun_tourist_id IS NOT NULL THEN
        INSERT INTO recommendation (tourist_id, location_id, recommendation_reason, created_at)
        VALUES (kasun_tourist_id, spot_main_point, 'Matches your BEGINNER skill level, safety rating 5/5, current wave height 0.8m', now());
    END IF;
END $$;
