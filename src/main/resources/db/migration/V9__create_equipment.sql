CREATE TABLE equipment (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL REFERENCES surf_schools(id),
    type VARCHAR(20) NOT NULL,
    name VARCHAR(150) NOT NULL,
    size VARCHAR(50),
    total_quantity INTEGER NOT NULL,
    rental_price_per_hour NUMERIC(10,2) NOT NULL,
    rental_price_per_day NUMERIC(10,2) NOT NULL,
    available BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_equipment_school ON equipment (school_id);
