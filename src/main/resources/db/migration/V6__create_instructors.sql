CREATE TABLE instructors (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL REFERENCES surf_schools(id),
    name VARCHAR(150) NOT NULL,
    bio VARCHAR(2000),
    photo_url VARCHAR(500),
    experience_years INTEGER,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_instructors_school ON instructors (school_id);

CREATE TABLE instructor_certifications (
    instructor_id BIGINT NOT NULL REFERENCES instructors(id) ON DELETE CASCADE,
    certification VARCHAR(255) NOT NULL
);
CREATE INDEX idx_instructor_certifications_instructor ON instructor_certifications (instructor_id);

CREATE TABLE instructor_languages (
    instructor_id BIGINT NOT NULL REFERENCES instructors(id) ON DELETE CASCADE,
    language VARCHAR(100) NOT NULL
);
CREATE INDEX idx_instructor_languages_instructor ON instructor_languages (instructor_id);

CREATE TABLE instructor_skill_levels (
    instructor_id BIGINT NOT NULL REFERENCES instructors(id) ON DELETE CASCADE,
    skill_level VARCHAR(20) NOT NULL
);
CREATE INDEX idx_instructor_skill_levels_instructor ON instructor_skill_levels (instructor_id);
