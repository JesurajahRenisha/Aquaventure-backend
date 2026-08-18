-- Seed data so the frontend has something to render immediately.
-- All seeded accounts use the password: Passw0rd!
--
-- Written as a single PL/pgSQL block using RETURNING ... INTO variables
-- (never hardcoded ids) because the `users` table may already contain rows
-- carried over from a legacy schema (see V1) -- hardcoding id=1..5 would
-- collide with those existing primary keys.

DO $$
DECLARE
    admin_id BIGINT;
    ravi_id BIGINT;
    priya_id BIGINT;
    kasun_id BIGINT;
    amaya_id BIGINT;
    ravi_profile_id BIGINT;
    priya_profile_id BIGINT;
    spot_main_point BIGINT;
    spot_peanut_farm BIGINT;
    spot_whiskey_point BIGINT;
    lesson_beginner BIGINT;
    lesson_intermediate BIGINT;
    lesson_advanced BIGINT;
    slot1 BIGINT;
    slot2 BIGINT;
    slot3 BIGINT;
    slot4 BIGINT;
    booking1 BIGINT;
    booking2 BIGINT;
    booking3 BIGINT;
    booking4 BIGINT;
    conversation1 BIGINT;
    password_hash CONSTANT VARCHAR := '$2a$10$C1rXpJWO7/dwYiNqVC3Ih.hSFqTnKL1Gr21o97fMS2XDcBla31/VC';
BEGIN
    -- Users
    INSERT INTO users (firstname, lastname, email, password, role, enabled, created_at)
    VALUES ('Admin', 'User', 'admin@aquaventure.com', password_hash, 'ADMIN', TRUE, now() - INTERVAL '90 days')
    RETURNING id INTO admin_id;

    INSERT INTO users (firstname, lastname, email, password, role, enabled, created_at)
    VALUES ('Ravi', 'Fernando', 'ravi@aquaventure.com', password_hash, 'PROVIDER', TRUE, now() - INTERVAL '60 days')
    RETURNING id INTO ravi_id;

    INSERT INTO users (firstname, lastname, email, password, role, enabled, created_at)
    VALUES ('Priya', 'De Silva', 'priya@aquaventure.com', password_hash, 'PROVIDER', TRUE, now() - INTERVAL '45 days')
    RETURNING id INTO priya_id;

    INSERT INTO users (firstname, lastname, email, password, role, enabled, created_at)
    VALUES ('Kasun', 'Perera', 'kasun@example.com', password_hash, 'SURFER', TRUE, now() - INTERVAL '30 days')
    RETURNING id INTO kasun_id;

    INSERT INTO users (firstname, lastname, email, password, role, enabled, created_at)
    VALUES ('Amaya', 'Silva', 'amaya@example.com', password_hash, 'SURFER', TRUE, now() - INTERVAL '20 days')
    RETURNING id INTO amaya_id;

    -- Provider profiles
    INSERT INTO provider_profiles (user_id, business_name, bio, verification_status, applied_at, decided_at, decided_by_admin_id)
    VALUES (ravi_id, 'Ravi''s Wave School', 'Certified instructors teaching at Main Point since 2014.', 'APPROVED', now() - INTERVAL '60 days', now() - INTERVAL '55 days', admin_id)
    RETURNING id INTO ravi_profile_id;

    INSERT INTO provider_profiles (user_id, business_name, bio, verification_status, applied_at, decided_at, decided_by_admin_id)
    VALUES (priya_id, 'Bay Break Surf Co.', 'Small-group coaching for intermediate and advanced surfers.', 'APPROVED', now() - INTERVAL '45 days', now() - INTERVAL '40 days', admin_id)
    RETURNING id INTO priya_profile_id;

    -- Surf spots
    INSERT INTO surf_spots (name, region, description, difficulty_level, image_url)
    VALUES ('Main Point', 'Arugam Bay', 'Long, forgiving right-hand point break, ideal for beginners.', 'BEGINNER', NULL)
    RETURNING id INTO spot_main_point;

    INSERT INTO surf_spots (name, region, description, difficulty_level, image_url)
    VALUES ('Peanut Farm', 'Arugam Bay', 'Mellow reef break a short boat ride from Main Point.', 'INTERMEDIATE', NULL)
    RETURNING id INTO spot_peanut_farm;

    INSERT INTO surf_spots (name, region, description, difficulty_level, image_url)
    VALUES ('Whiskey Point', 'Arugam Bay', 'Fast, punchy point break for experienced surfers.', 'ADVANCED', NULL)
    RETURNING id INTO spot_whiskey_point;

    -- Lessons
    INSERT INTO lessons (provider_profile_id, surf_spot_id, title, description, level, duration_minutes, capacity, price, active)
    VALUES (ravi_profile_id, spot_main_point, 'Beginner Surf Lesson', 'Learn to catch your first wave with full safety support.', 'BEGINNER', 120, 4, 25.00, TRUE)
    RETURNING id INTO lesson_beginner;

    INSERT INTO lessons (provider_profile_id, surf_spot_id, title, description, level, duration_minutes, capacity, price, active)
    VALUES (ravi_profile_id, spot_peanut_farm, 'Intermediate Wave Coaching', 'Improve take-offs and turns in small groups.', 'INTERMEDIATE', 150, 3, 35.00, TRUE)
    RETURNING id INTO lesson_intermediate;

    INSERT INTO lessons (provider_profile_id, surf_spot_id, title, description, level, duration_minutes, capacity, price, active)
    VALUES (priya_profile_id, spot_whiskey_point, 'Advanced Point Break Session', 'Push your limits at one of Arugam Bay''s best breaks.', 'ADVANCED', 180, 2, 45.00, TRUE)
    RETURNING id INTO lesson_advanced;

    -- Availability slots
    INSERT INTO availability_slots (lesson_id, date, start_time, end_time, capacity, booked_count, status)
    VALUES (lesson_beginner, CURRENT_DATE + 1, '08:00', '10:00', 4, 1, 'OPEN')
    RETURNING id INTO slot1;

    INSERT INTO availability_slots (lesson_id, date, start_time, end_time, capacity, booked_count, status)
    VALUES (lesson_beginner, CURRENT_DATE - 7, '08:00', '10:00', 4, 2, 'FULL')
    RETURNING id INTO slot2;

    INSERT INTO availability_slots (lesson_id, date, start_time, end_time, capacity, booked_count, status)
    VALUES (lesson_intermediate, CURRENT_DATE + 3, '09:00', '11:30', 3, 1, 'OPEN')
    RETURNING id INTO slot3;

    INSERT INTO availability_slots (lesson_id, date, start_time, end_time, capacity, booked_count, status)
    VALUES (lesson_advanced, CURRENT_DATE - 14, '07:00', '10:00', 2, 1, 'FULL')
    RETURNING id INTO slot4;

    -- Bookings
    INSERT INTO bookings (surfer_id, lesson_id, slot_id, num_guests, total_price, status, cancellation_reason, refund_percentage, created_at)
    VALUES (kasun_id, lesson_beginner, slot1, 1, 25.00, 'CONFIRMED', NULL, NULL, now() - INTERVAL '2 days')
    RETURNING id INTO booking1;

    INSERT INTO bookings (surfer_id, lesson_id, slot_id, num_guests, total_price, status, cancellation_reason, refund_percentage, created_at)
    VALUES (kasun_id, lesson_beginner, slot2, 2, 50.00, 'COMPLETED', NULL, NULL, now() - INTERVAL '10 days')
    RETURNING id INTO booking2;

    INSERT INTO bookings (surfer_id, lesson_id, slot_id, num_guests, total_price, status, cancellation_reason, refund_percentage, created_at)
    VALUES (amaya_id, lesson_intermediate, slot3, 1, 35.00, 'PENDING', NULL, NULL, now() - INTERVAL '1 days')
    RETURNING id INTO booking3;

    INSERT INTO bookings (surfer_id, lesson_id, slot_id, num_guests, total_price, status, cancellation_reason, refund_percentage, created_at)
    VALUES (amaya_id, lesson_advanced, slot4, 1, 45.00, 'CANCELLED', 'Unsafe weather conditions on the day', 100, now() - INTERVAL '16 days')
    RETURNING id INTO booking4;

    -- Review
    INSERT INTO reviews (booking_id, surfer_id, provider_profile_id, rating, comment, moderation_status, created_at)
    VALUES (booking2, kasun_id, ravi_profile_id, 5, 'Amazing first lesson, Ravi was patient and encouraging!', 'PUBLISHED', now() - INTERVAL '9 days');

    -- Surf sessions (self-logged, separate from bookings)
    INSERT INTO surf_sessions (surfer_id, surf_spot_id, booking_id, session_date, duration_minutes, wave_height_m, notes)
    VALUES (kasun_id, spot_main_point, booking2, CURRENT_DATE - 10, 120, 1.2, 'Caught my first green wave!');

    INSERT INTO surf_sessions (surfer_id, surf_spot_id, booking_id, session_date, duration_minutes, wave_height_m, notes)
    VALUES (amaya_id, spot_whiskey_point, NULL, CURRENT_DATE - 3, 60, 1.8, 'Solo session, choppy conditions.');

    -- Favorite spots
    INSERT INTO favorite_spots (surfer_id, surf_spot_id, created_at)
    VALUES (kasun_id, spot_main_point, now() - INTERVAL '25 days');

    INSERT INTO favorite_spots (surfer_id, surf_spot_id, created_at)
    VALUES (amaya_id, spot_whiskey_point, now() - INTERVAL '15 days');

    -- Surf conditions (latest snapshot per spot)
    INSERT INTO surf_conditions (surf_spot_id, recorded_at, wave_height_m, wind_speed_kmh, tide, uv_index, visibility, temperature_c, safety_flag)
    VALUES (spot_main_point, now(), 1.2, 12, 'High', 8, 'Excellent', 29, 'SAFE');

    INSERT INTO surf_conditions (surf_spot_id, recorded_at, wave_height_m, wind_speed_kmh, tide, uv_index, visibility, temperature_c, safety_flag)
    VALUES (spot_peanut_farm, now(), 1.6, 18, 'Mid', 9, 'Good', 28, 'CAUTION');

    INSERT INTO surf_conditions (surf_spot_id, recorded_at, wave_height_m, wind_speed_kmh, tide, uv_index, visibility, temperature_c, safety_flag)
    VALUES (spot_whiskey_point, now(), 2.4, 25, 'Low', 7, 'Fair', 27, 'UNSAFE');

    -- Notifications
    INSERT INTO notifications (user_id, type, title, message, is_read, created_at)
    VALUES (kasun_id, 'BOOKING_CONFIRMED', 'Booking confirmed', 'Your Beginner Surf Lesson at Main Point is confirmed.', FALSE, now() - INTERVAL '2 days');

    INSERT INTO notifications (user_id, type, title, message, is_read, created_at)
    VALUES (ravi_id, 'REVIEW_RECEIVED', 'New review', 'You received a 5-star review from Kasun.', FALSE, now() - INTERVAL '9 days');

    INSERT INTO notifications (user_id, type, title, message, is_read, created_at)
    VALUES (admin_id, 'GENERAL', 'Weekly summary', 'Platform activity summary is ready to view.', TRUE, now() - INTERVAL '1 days');

    -- Conversation + messages
    INSERT INTO conversations (participant_a_id, participant_b_id, created_at)
    VALUES (kasun_id, ravi_id, now() - INTERVAL '3 hours')
    RETURNING id INTO conversation1;

    INSERT INTO messages (conversation_id, sender_id, body, sent_at, read_at)
    VALUES (conversation1, kasun_id, 'Hi, is the 8am slot still open tomorrow?', now() - INTERVAL '3 hours', now() - INTERVAL '2 hours 50 minutes');

    INSERT INTO messages (conversation_id, sender_id, body, sent_at, read_at)
    VALUES (conversation1, ravi_id, 'Yes! See you there.', now() - INTERVAL '2 hours', NULL);

    -- Platform settings
    INSERT INTO platform_settings (setting_key, setting_value)
    VALUES ('platform_name', 'AquaVenture');

    INSERT INTO platform_settings (setting_key, setting_value)
    VALUES ('cancellation_policy', 'full_refund_hours=48;partial_refund_hours=24;partial_refund_percentage=50');
END $$;
