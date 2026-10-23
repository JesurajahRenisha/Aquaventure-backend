CREATE TABLE surf_schools (
    id BIGSERIAL PRIMARY KEY,
    owner_user_id BIGINT NOT NULL UNIQUE REFERENCES users(id),
    name VARCHAR(150) NOT NULL,
    description VARCHAR(2000),
    address VARCHAR(255),
    contact_phone VARCHAR(30),
    contact_email VARCHAR(100),
    business_registration_number VARCHAR(100),
    logo_url VARCHAR(500),
    status VARCHAR(20) NOT NULL,
    rejection_reason VARCHAR(1000),
    approved_at TIMESTAMPTZ,
    approved_by_user_id BIGINT REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_surf_schools_status ON surf_schools (status);
CREATE INDEX idx_surf_schools_owner ON surf_schools (owner_user_id);

CREATE TABLE surf_school_documents (
    surf_school_id BIGINT NOT NULL REFERENCES surf_schools(id) ON DELETE CASCADE,
    document_url VARCHAR(500) NOT NULL
);

CREATE INDEX idx_surf_school_documents_school ON surf_school_documents (surf_school_id);

CREATE TABLE surf_school_images (
    surf_school_id BIGINT NOT NULL REFERENCES surf_schools(id) ON DELETE CASCADE,
    image_url VARCHAR(500) NOT NULL
);

CREATE INDEX idx_surf_school_images_school ON surf_school_images (surf_school_id);

CREATE TABLE surf_school_locations (
    surf_school_id BIGINT NOT NULL REFERENCES surf_schools(id) ON DELETE CASCADE,
    surf_location_id BIGINT NOT NULL REFERENCES surf_locations(id) ON DELETE CASCADE,
    PRIMARY KEY (surf_school_id, surf_location_id)
);
